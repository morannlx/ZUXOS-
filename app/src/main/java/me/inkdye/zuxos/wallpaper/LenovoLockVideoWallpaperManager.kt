package me.inkdye.zuxos.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import me.inkdye.zuxos.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.inkdye.zuxos.root.RootCommandResult
import me.inkdye.zuxos.root.RootManager
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.UUID

object LenovoLockVideoWallpaperManager {
    private const val KEYGUARD_ACTION = "com.lenovo.keyguard.set_keyguard_wallpaper"
    private const val LIBRARY_DIR = "/storage/emulated/0/.keyguard/zuxos_lock_video_wallpaper"
    private const val CUSTOMIZED_DIR = "/storage/emulated/0/.keyguard/customized_lockscreen"
    private const val VIDEO_DIR = "/storage/emulated/0/.keyguard/video"
    private const val PREVIEW_NAME = "wallpaper"
    private const val PREVIEW_LAND_NAME = "wallpaper_land"
    private const val VIDEO_PORT_NAME = "wallpaper_port.mp4"
    private const val VIDEO_LAND_NAME = "wallpaper_land.mp4"
    private const val DESKTOP_LIVE_DIR_NAME = "desktop_live_wallpaper"
    private const val DESKTOP_LIVE_LIBRARY_DIR_NAME = "desktop_live_wallpaper_library"
    private const val ACTIVE_DESKTOP_ENTRY_NAME = "active_desktop_entry"
    private const val MAX_PREVIEW_EDGE = 1440
    private const val MODULE_DATA_DIR = "/data/user/0/${BuildConfig.APPLICATION_ID}"

    const val WALLPAPER_SETTING_PACKAGE = "com.zui.wallpapersetting"
    const val CUSTOM_ITEM_PACKAGE = "me.inkdye.zuxos.custom.lockvideo"
    const val CUSTOM_ITEM_NAME = "zuxos_custom_lock_video"
    private const val CUSTOM_ITEM_PREFIX = "${CUSTOM_ITEM_NAME}_"

    enum class ApplyMethod {
        DIRECT,
        ROOT,
    }

    enum class DesktopApplyMethod {
        DIRECT,
        ROOT,
        CONFIRMATION_INTENT,
    }

    data class ImportResult(
        val method: ApplyMethod,
        val videoPath: String,
    )

    data class LibraryEntry(
        val id: String,
        val directory: String,
        val videoPath: String,
        val videoLandPath: String,
        val previewPath: String,
        val previewLandPath: String,
        val lastModified: Long,
    )

    suspend fun importVideo(context: Context, source: Uri): ImportResult = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val workDir = File(appContext.cacheDir, "lock_video_wallpaper").apply {
            deleteRecursively()
            mkdirs()
        }
        val videoTemp = File(workDir, VIDEO_PORT_NAME)
        val previewTemp = File(workDir, PREVIEW_NAME)
        val entryId = newEntryId()

        copyVideoToTemp(appContext, source, videoTemp)
        writePreview(videoTemp, previewTemp)
        installDesktopPrivateCopy(appContext, entryId, videoTemp, previewTemp)

        val method = runCatching {
            installLibraryDirect(entryId, videoTemp, previewTemp)
            ApplyMethod.DIRECT
        }.getOrElse { directError ->
            val rootResult = installLibraryWithRoot(entryId, videoTemp, previewTemp)
            if (!rootResult.success) {
                error(
                    buildString {
                        append("Direct write failed: ")
                        append(directError.message ?: directError.javaClass.simpleName)
                        append("; Root write failed: ")
                        append(rootResult.message)
                    },
                )
            }
            ApplyMethod.ROOT
        }

        ImportResult(method = method, videoPath = "$LIBRARY_DIR/$entryId/$VIDEO_PORT_NAME")
    }

    fun activateImportedVideo(context: Context, entryId: String? = null): ApplyMethod {
        val entry = entryForItemName(entryId) ?: error("No imported lock video found")
        val video = File(entry.videoPath)
        val videoLand = File(entry.videoLandPath).takeIf { it.isFile } ?: video
        val preview = File(entry.previewPath)
        val previewLand = File(entry.previewLandPath).takeIf { it.isFile } ?: preview
        check(video.isFile) { "No imported lock video found" }
        check(preview.isFile) { "No imported lock video preview found" }

        val method = runCatching {
            installActiveDirect(video, videoLand, preview, previewLand)
            ApplyMethod.DIRECT
        }.getOrElse { directError ->
            val rootResult = runBlocking {
                installActiveWithRoot(video, videoLand, preview, previewLand)
            }
            if (!rootResult.success) {
                error(
                    buildString {
                        append("Direct write failed: ")
                        append(directError.message ?: directError.javaClass.simpleName)
                        append("; Root write failed: ")
                        append(rootResult.message)
                    },
                )
            }
            ApplyMethod.ROOT
        }
        notifySystemUi(context.applicationContext)
        return method
    }

    fun activateDesktopLiveWallpaper(context: Context, entryId: String? = null): DesktopApplyMethod {
        val entry = entryForItemName(entryId) ?: error("No imported desktop video found")
        check(File(entry.videoPath).isFile) { "No imported desktop video found" }
        prepareDesktopLiveVideo(context, entry)

        val component = ComponentName(
            BuildConfig.APPLICATION_ID,
            ZuxosVideoWallpaperService::class.java.name,
        )
        val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
        val directAppliedWithFlags = runCatching {
            WallpaperManager::class.java
                .getMethod(
                    "setWallpaperComponentWithFlags",
                    ComponentName::class.java,
                    Int::class.javaPrimitiveType,
                )
                .invoke(wallpaperManager, component, WallpaperManager.FLAG_SYSTEM)
        }.isSuccess
        if (directAppliedWithFlags) return DesktopApplyMethod.DIRECT

        val directApplied = runCatching {
            WallpaperManager::class.java
                .getMethod("setWallpaperComponent", ComponentName::class.java)
                .invoke(wallpaperManager, component)
        }.isSuccess
        if (directApplied) return DesktopApplyMethod.DIRECT

        val rootApplied = runBlocking {
            setDesktopLiveWallpaperWithRoot(component)
        }.success
        if (rootApplied) return DesktopApplyMethod.ROOT

        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return DesktopApplyMethod.CONFIRMATION_INTENT
    }

    fun hasImportedVideo(): Boolean = listEntries().isNotEmpty()

    fun activeDesktopEntry(): LibraryEntry? {
        val id = File(LIBRARY_DIR, ACTIVE_DESKTOP_ENTRY_NAME)
            .takeIf { it.isFile }
            ?.readText()
            ?.trim()
            .orEmpty()
        return entryForItemName(id)
    }

    fun activeDesktopVideoFile(context: Context): File =
        activeDesktopEntryId()
            ?.let { File(context.filesDir, "$DESKTOP_LIVE_LIBRARY_DIR_NAME/$it/$VIDEO_PORT_NAME") }
            ?.takeIf { it.isFile }
            ?: activeDesktopEntry()?.videoPath?.let(::File)?.takeIf { it.isFile }
            ?: File(context.filesDir, "$DESKTOP_LIVE_DIR_NAME/$VIDEO_PORT_NAME")

    fun activeDesktopPreviewFile(context: Context): File? =
        activeDesktopEntryId()
            ?.let { File(context.filesDir, "$DESKTOP_LIVE_LIBRARY_DIR_NAME/$it/$PREVIEW_NAME") }
            ?.takeIf { it.isFile }
            ?: activeDesktopEntry()?.previewPath?.let(::File)?.takeIf { it.isFile }

    private fun activeDesktopEntryId(): String? =
        File(LIBRARY_DIR, ACTIVE_DESKTOP_ENTRY_NAME)
            .takeIf { it.isFile }
            ?.readText()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    fun listEntries(): List<LibraryEntry> {
        val libraryDir = File(LIBRARY_DIR)
        if (!libraryDir.isDirectory) return emptyList()
        val entries = libraryDir.listFiles()
            ?.filter { it.isDirectory && File(it, VIDEO_PORT_NAME).isFile }
            ?.map { it.toLibraryEntry(it.name) }
            .orEmpty()
            .sortedByDescending { it.lastModified }
        if (entries.isNotEmpty()) return entries

        val legacyVideo = File(libraryDir, VIDEO_PORT_NAME)
        return if (legacyVideo.isFile) {
            listOf(libraryDir.toLibraryEntry(CUSTOM_ITEM_NAME))
        } else {
            emptyList()
        }
    }

    fun entryForItemName(itemName: String?): LibraryEntry? {
        val entries = listEntries()
        if (entries.isEmpty()) return null
        if (itemName.isNullOrBlank() || itemName == "ZUXOS+") return entries.firstOrNull()
        return entries.firstOrNull { it.id == itemName } ?: entries.firstOrNull {
            itemName == CUSTOM_ITEM_NAME && it.id == CUSTOM_ITEM_NAME
        }
    }

    fun isCustomItemName(itemName: String?): Boolean =
        itemName == CUSTOM_ITEM_NAME || itemName?.startsWith(CUSTOM_ITEM_PREFIX) == true || itemName == "ZUXOS+"

    fun previewPath(entryId: String? = null): String =
        entryForItemName(entryId)?.previewPath ?: "$LIBRARY_DIR/$PREVIEW_NAME"

    fun videoPath(entryId: String? = null, landscape: Boolean = false): String =
        entryForItemName(entryId)?.let {
            if (landscape) it.videoLandPath else it.videoPath
        } ?: "$LIBRARY_DIR/${if (landscape) VIDEO_LAND_NAME else VIDEO_PORT_NAME}"

    private fun setActiveDesktopEntry(entryId: String) {
        val libraryDir = File(LIBRARY_DIR)
        if (!libraryDir.exists() && !libraryDir.mkdirs()) {
            error("Cannot create $LIBRARY_DIR")
        }
        File(libraryDir, ACTIVE_DESKTOP_ENTRY_NAME).writeText(entryId)
    }

    private fun prepareDesktopLiveVideo(context: Context, entry: LibraryEntry) {
        val moduleContext = runCatching {
            context.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY,
            )
        }.getOrNull()
        val moduleFilesDir = moduleContext?.filesDir ?: File(MODULE_DATA_DIR, "files")
        val privateVideo = File(moduleFilesDir, "$DESKTOP_LIVE_LIBRARY_DIR_NAME/${entry.id}/$VIDEO_PORT_NAME")
        check(privateVideo.isFile || File(entry.videoPath).isFile) {
            "No playable desktop video found"
        }
        setActiveDesktopEntryBestEffort(entry.id)
    }

    private fun setActiveDesktopEntryBestEffort(entryId: String) {
        if (runCatching { setActiveDesktopEntry(entryId) }.isSuccess) return

        val marker = "$LIBRARY_DIR/$ACTIVE_DESKTOP_ENTRY_NAME"
        val command = """
            mkdir -p ${LIBRARY_DIR.sh()} &&
            printf %s ${entryId.sh()} > ${marker.sh()} &&
            chmod 664 ${marker.sh()} 2>/dev/null || true
        """.trimIndent()
        val rootResult = runBlocking {
            RootManager.execute(command, timeoutSeconds = 10)
        }
        if (!rootResult.success) error(rootResult.message)
    }

    private fun installDesktopPrivateCopy(context: Context, entryId: String, video: File, preview: File) {
        val entryDir = File(context.filesDir, "$DESKTOP_LIVE_LIBRARY_DIR_NAME/$entryId").apply {
            deleteRecursively()
            mkdirs()
        }
        val desktopVideo = File(entryDir, VIDEO_PORT_NAME)
        runCatching {
            writeVideoOnlyMp4(video, desktopVideo)
        }.getOrElse {
            video.copyTo(desktopVideo, overwrite = true)
        }
        preview.copyTo(File(entryDir, PREVIEW_NAME), overwrite = true)
    }

    private fun writeVideoOnlyMp4(input: File, output: File) {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        try {
            extractor.setDataSource(input.absolutePath)
            val videoTrack = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index)
                    .getString(android.media.MediaFormat.KEY_MIME)
                    ?.startsWith("video/") == true
            } ?: error("No video track")
            extractor.selectTrack(videoTrack)
            val format = extractor.getTrackFormat(videoTrack)
            muxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrack = muxer.addTrack(format)
            muxer.start()

            val maxInputSize = runCatching {
                format.getInteger(android.media.MediaFormat.KEY_MAX_INPUT_SIZE)
            }.getOrDefault(1024 * 1024)
            val buffer = ByteBuffer.allocate(maxInputSize.coerceAtLeast(1024 * 1024))
            val info = MediaCodec.BufferInfo()
            while (true) {
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break
                info.set(
                    0,
                    size,
                    extractor.sampleTime,
                    extractor.sampleFlags,
                )
                muxer.writeSampleData(muxerTrack, buffer, info)
                extractor.advance()
            }
        } finally {
            runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
            runCatching { extractor.release() }
        }
        check(output.length() > 0L) { "Desktop video remux failed" }
    }

    private suspend fun setDesktopLiveWallpaperWithRoot(component: ComponentName): RootCommandResult {
        val componentName = component.flattenToString().sh()
        val command = """
            cmd wallpaper set-live-wallpaper $componentName ||
            cmd wallpaper set-live-wallpaper --user 0 $componentName
        """.trimIndent()
        return RootManager.execute(command, timeoutSeconds = 15)
    }

    private fun copyVideoToTemp(context: Context, source: Uri, output: File) {
        context.contentResolver.openInputStream(source)?.use { input ->
            FileOutputStream(output).use { fileOutput ->
                input.copyTo(fileOutput, bufferSize = DEFAULT_BUFFER_SIZE)
            }
        } ?: error("Cannot open selected video")
        if (output.length() <= 0L) error("Selected video is empty")
    }

    private fun writePreview(video: File, output: File) {
        val frame = MediaMetadataRetriever().useFrame(video)
        val preview = frame?.fitWithin(MAX_PREVIEW_EDGE)
            ?: Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.BLACK)
            }
        FileOutputStream(output).use { stream ->
            check(preview.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                "Failed to encode preview image"
            }
        }
        if (preview !== frame) preview.recycle()
        frame?.recycle()
    }

    private fun MediaMetadataRetriever.useFrame(video: File): Bitmap? {
        return try {
            setDataSource(video.absolutePath)
            val durationUs = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.times(1000L)
                ?: 0L
            listOf(0L, 500_000L, 1_000_000L, durationUs / 2)
                .distinct()
                .firstNotNullOfOrNull { timeUs ->
                    getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                }
        } finally {
            runCatching { release() }
        }
    }

    private fun Bitmap.fitWithin(maxEdge: Int): Bitmap {
        val edge = maxOf(width, height)
        if (edge <= maxEdge) return this
        val scale = maxEdge.toFloat() / edge.toFloat()
        return Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true,
        )
    }

    private fun installLibraryDirect(entryId: String, videoTemp: File, previewTemp: File) {
        val libraryDir = File(LIBRARY_DIR)
        if (!libraryDir.exists() && !libraryDir.mkdirs()) {
            error("Cannot create $LIBRARY_DIR")
        }
        val entryDir = File(libraryDir, entryId)
        if (!entryDir.exists() && !entryDir.mkdirs()) {
            error("Cannot create ${entryDir.absolutePath}")
        }

        val writeTest = File(entryDir, ".zuxos_write_test")
        writeTest.writeText("")
        writeTest.delete()

        previewTemp.copyTo(File(entryDir, PREVIEW_NAME), overwrite = true)
        previewTemp.copyTo(File(entryDir, PREVIEW_LAND_NAME), overwrite = true)
        videoTemp.copyTo(File(entryDir, VIDEO_PORT_NAME), overwrite = true)
        videoTemp.copyTo(File(entryDir, VIDEO_LAND_NAME), overwrite = true)
    }

    private fun installActiveDirect(video: File, videoLand: File, preview: File, previewLand: File) {
        val customizedDir = File(CUSTOMIZED_DIR)
        val videoDir = File(VIDEO_DIR)
        if (!customizedDir.exists() && !customizedDir.mkdirs()) {
            error("Cannot create $CUSTOMIZED_DIR")
        }
        if (!videoDir.exists() && !videoDir.mkdirs()) {
            error("Cannot create $VIDEO_DIR")
        }

        val writeTest = File(videoDir, ".zuxos_write_test")
        writeTest.writeText("")
        writeTest.delete()

        customizedDir.listFiles()?.forEach { it.deleteRecursively() }
        videoDir.listFiles()?.forEach { it.deleteRecursively() }
        preview.copyTo(File(customizedDir, PREVIEW_NAME), overwrite = true)
        previewLand.copyTo(File(customizedDir, PREVIEW_LAND_NAME), overwrite = true)
        video.copyTo(File(videoDir, VIDEO_PORT_NAME), overwrite = true)
        videoLand.copyTo(File(videoDir, VIDEO_LAND_NAME), overwrite = true)
    }

    private suspend fun installLibraryWithRoot(entryId: String, videoTemp: File, previewTemp: File): RootCommandResult {
        val entryDir = "$LIBRARY_DIR/$entryId"
        val command = """
            mkdir -p ${entryDir.sh()} &&
            cp ${previewTemp.absolutePath.sh()} ${(entryDir + "/" + PREVIEW_NAME).sh()} &&
            cp ${previewTemp.absolutePath.sh()} ${(entryDir + "/" + PREVIEW_LAND_NAME).sh()} &&
            cp ${videoTemp.absolutePath.sh()} ${(entryDir + "/" + VIDEO_PORT_NAME).sh()} &&
            cp ${videoTemp.absolutePath.sh()} ${(entryDir + "/" + VIDEO_LAND_NAME).sh()} &&
            chmod 664 ${(entryDir + "/" + PREVIEW_NAME).sh()} ${(entryDir + "/" + PREVIEW_LAND_NAME).sh()} ${(entryDir + "/" + VIDEO_PORT_NAME).sh()} ${(entryDir + "/" + VIDEO_LAND_NAME).sh()} 2>/dev/null || true
        """.trimIndent()
        return RootManager.execute(command, timeoutSeconds = 30)
    }

    private suspend fun installActiveWithRoot(video: File, videoLand: File, preview: File, previewLand: File): RootCommandResult {
        val command = """
            mkdir -p ${CUSTOMIZED_DIR.sh()} ${VIDEO_DIR.sh()} &&
            rm -rf ${CUSTOMIZED_DIR.sh()}/* ${CUSTOMIZED_DIR.sh()}/.[!.]* ${CUSTOMIZED_DIR.sh()}/..?* 2>/dev/null || true;
            rm -rf ${VIDEO_DIR.sh()}/* ${VIDEO_DIR.sh()}/.[!.]* ${VIDEO_DIR.sh()}/..?* 2>/dev/null || true;
            cp ${preview.absolutePath.sh()} ${(CUSTOMIZED_DIR + "/" + PREVIEW_NAME).sh()} &&
            cp ${previewLand.absolutePath.sh()} ${(CUSTOMIZED_DIR + "/" + PREVIEW_LAND_NAME).sh()} &&
            cp ${video.absolutePath.sh()} ${(VIDEO_DIR + "/" + VIDEO_PORT_NAME).sh()} &&
            cp ${videoLand.absolutePath.sh()} ${(VIDEO_DIR + "/" + VIDEO_LAND_NAME).sh()} &&
            chmod 664 ${(CUSTOMIZED_DIR + "/" + PREVIEW_NAME).sh()} ${(CUSTOMIZED_DIR + "/" + PREVIEW_LAND_NAME).sh()} ${(VIDEO_DIR + "/" + VIDEO_PORT_NAME).sh()} ${(VIDEO_DIR + "/" + VIDEO_LAND_NAME).sh()} 2>/dev/null || true
        """.trimIndent()
        return RootManager.execute(command, timeoutSeconds = 30)
    }

    private fun notifySystemUi(context: Context) {
        val intent = Intent(KEYGUARD_ACTION).apply {
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            putExtra("call_package", context.packageName)
            putExtra("title", "")
            putExtra("content", "")
            putExtra("url", "")
            putExtra("type", 3)
            putExtra("play_type", 0)
            putExtra("force", true)
        }
        context.sendBroadcast(intent)
    }

    private fun String.sh(): String = "'${replace("'", "'\"'\"'")}'"

    private fun newEntryId(): String = "$CUSTOM_ITEM_PREFIX${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"

    private fun File.toLibraryEntry(id: String): LibraryEntry = LibraryEntry(
        id = id,
        directory = absolutePath,
        videoPath = File(this, VIDEO_PORT_NAME).absolutePath,
        videoLandPath = File(this, VIDEO_LAND_NAME).absolutePath,
        previewPath = File(this, PREVIEW_NAME).absolutePath,
        previewLandPath = File(this, PREVIEW_LAND_NAME).absolutePath,
        lastModified = lastModified(),
    )
}
