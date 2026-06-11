package me.inkdye.zuxos.hook.lenovo

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import me.inkdye.zuxos.wallpaper.LenovoLockVideoWallpaperManager
import java.io.File

object LenovoWallpaperSettingCustomVideoHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_WALLPAPER_CUSTOM_LOCK_VIDEO
    override val targetPackage = LenovoLockVideoWallpaperManager.WALLPAPER_SETTING_PACKAGE
    override val moduleName = "lenovo_wallpaper_custom_lock_video"
    override val alwaysRegister = true

    private val applyDialogPreview = ThreadLocal<Any?>()

    override fun PackageParam.onHook() {
        hookLocalDynamicList(appClassLoader)
        hookDynamicSectionInit(appClassLoader)
        hookDynamicAdapterType(appClassLoader)
        hookDynamicThumbnail(appClassLoader)
        hookPreviewResources(appClassLoader)
        hookPreviewVideo(appClassLoader)
        hookApplyDialogMarker(appClassLoader)
        hookApplyDialogOption(appClassLoader)
        hookApplyCustomVideo(appClassLoader)
    }

    private fun hookLocalDynamicList(classLoader: ClassLoader?) {
        val activityClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.activity.LocallyWallpaperActivity",
            classLoader,
        ) ?: return
        XposedHelpers.findAndHookMethod(
            activityClass,
            "initSystemWallpapers",
            java.util.ArrayList::class.java,
            Boolean::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val isDynamic = param.args.getOrNull(1) as? Boolean ?: return
                    val list = param.args.getOrNull(0) as? MutableList<Any> ?: return
                    if (!isDynamic) {
                        removeCustomItems(list)
                        return
                    }
                    if (!LenovoLockVideoWallpaperManager.hasImportedVideo()) return
                    ensureCustomItems(list, classLoader)
                }
            },
        )
    }

    private fun createCustomLocalPic(
        classLoader: ClassLoader?,
        entry: LenovoLockVideoWallpaperManager.LibraryEntry,
    ): Any? = runCatching {
        val localPicClass = XposedHelpers.findClass(
            "com.zui.wallpapersetting.domain.LocalPic",
            classLoader,
        )
        localPicClass.getConstructor(
            Resources::class.java,
            String::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            String::class.java,
            Int::class.javaPrimitiveType,
        ).newInstance(
            null,
            LenovoLockVideoWallpaperManager.CUSTOM_ITEM_PACKAGE,
            0,
            0,
            0,
            0,
            0,
            entry.id,
            2,
        ).also { localPic ->
            XposedHelpers.callMethod(localPic, "setName", entry.id)
        }
    }.getOrNull()

    private fun hookDynamicSectionInit(classLoader: ClassLoader?) {
        val activityClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.activity.LocallyWallpaperActivity",
            classLoader,
        ) ?: return
        XposedHelpers.findAndHookMethod(
            activityClass,
            "initDynamicWallpaper",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val list = runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "dynamicInfos") as? MutableList<Any>
                    }.getOrNull() ?: return
                    ensureCustomItems(list, classLoader)
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    val adapter = runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "dynamicAdapter") as? RecyclerView.Adapter<*>
                    }.getOrNull()
                    adapter?.notifyDataSetChanged()
                }
            },
        )
        XposedHelpers.findAndHookMethod(
            activityClass,
            "initSystemPreloadWallpaper",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    cleanupStaticLists(param.thisObject)
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    cleanupStaticLists(param.thisObject)
                    val adapter = runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "systemAdapter") as? RecyclerView.Adapter<*>
                    }.getOrNull()
                    adapter?.notifyDataSetChanged()
                }
            },
        )
    }

    private fun hookDynamicAdapterType(classLoader: ClassLoader?) {
        val adapterClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.adapter.ElectricityAdapter",
            classLoader,
        ) ?: return
        XposedHelpers.findAndHookMethod(
            adapterClass,
            "setType",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val type = param.args.getOrNull(0) as? Int ?: return
                    if (type != 2 || !LenovoLockVideoWallpaperManager.hasImportedVideo()) return
                    val list = runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "data") as? MutableList<Any>
                    }.getOrNull() ?: return
                    if (ensureCustomItems(list, classLoader)) {
                        (param.thisObject as? RecyclerView.Adapter<*>)?.notifyDataSetChanged()
                    }
                }
            },
        )
    }

    private fun ensureCustomItems(list: MutableList<Any>, classLoader: ClassLoader?): Boolean {
        val entries = LenovoLockVideoWallpaperManager.listEntries()
        if (entries.isEmpty()) return false

        var changed = false
        val existingIds = list.mapNotNull { it.customLocalPicId() }.toSet()
        entries.asReversed().forEach { entry ->
            if (entry.id !in existingIds) {
                val item = createCustomLocalPic(classLoader, entry) ?: return@forEach
                list.add(0, item)
                changed = true
            }
        }
        return changed
    }

    private fun removeCustomItems(list: MutableList<Any>): Boolean {
        val before = list.size
        list.removeAll { it.isCustomLocalPic() }
        return list.size != before
    }

    private fun cleanupStaticLists(activity: Any?) {
        listOf("lists", "systemInfos").forEach { field ->
            runCatching {
                val list = XposedHelpers.getObjectField(activity, field) as? MutableList<Any>
                list?.let { removeCustomItems(it) }
            }
        }
    }

    private fun hookDynamicThumbnail(classLoader: ClassLoader?) {
        val adapterClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.adapter.ElectricityAdapter",
            classLoader,
        ) ?: return
        XposedHelpers.findAndHookMethod(
            adapterClass,
            "onBindViewHolder",
            "androidx.recyclerview.widget.RecyclerView.ViewHolder",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!param.thisObject.isDynamicWallpaperAdapter()) return
                    val data = runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "data") as? List<*>
                    }.getOrNull() ?: return
                    val index = param.args.getOrNull(1) as? Int ?: return
                    val item = data.getOrNull(index) ?: return
                    val entry = LenovoLockVideoWallpaperManager.entryForItemName(item.customLocalPicId())
                        ?: return
                    val imageView = runCatching {
                        XposedHelpers.getObjectField(param.args[0], "mElectricity_wallpaper") as? ImageView
                    }.getOrNull() ?: return
                    imageView.post {
                        imageView.setImageBitmap(decodePreview(entry.previewPath))
                    }
                    imageView.contentDescription = "ZUXOS+"
                }
            },
        )
    }

    private fun hookPreviewResources(classLoader: ClassLoader?) {
        val previewClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.activity.LocalWallpaperPreviewActivity",
            classLoader,
        ) ?: return
        XposedHelpers.findAndHookMethod(
            previewClass,
            "getSystemWallpapers",
            String::class.java,
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val packageName = param.args.getOrNull(0) as? String ?: return
                    if (packageName != LenovoLockVideoWallpaperManager.CUSTOM_ITEM_PACKAGE) return
                    val entry = selectedEntry(param.thisObject) ?: return
                    param.result = decodePreview(entry.previewPath)
                }
            },
        )
    }

    private fun hookPreviewVideo(classLoader: ClassLoader?) {
        val previewClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.activity.LocalWallpaperPreviewActivity",
            classLoader,
        ) ?: return
        XposedHelpers.findAndHookMethod(
            previewClass,
            "initVideoView",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!param.thisObject.isCustomPreview()) return
                    val activity = param.thisObject as? Activity ?: return
                    val videoView = XposedHelpers.getObjectField(param.thisObject, "mVideo") as? android.widget.VideoView
                        ?: return
                    val landscape = activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    val entry = selectedEntry(param.thisObject) ?: return
                    val video = File(if (landscape) entry.videoLandPath else entry.videoPath)
                    if (!video.isFile) return
                    videoView.setVideoURI(Uri.fromFile(video))
                    videoView.seekTo(0)
                    videoView.start()
                }
            },
        )
    }

    private fun hookApplyCustomVideo(classLoader: ClassLoader?) {
        val previewClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.activity.LocalWallpaperPreviewActivity",
            classLoader,
        ) ?: return
        listOf("setWallpaperScreen", "setWallpaperScreenH").forEach { methodName ->
            XposedHelpers.findAndHookMethod(
                previewClass,
                methodName,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!param.thisObject.isCustomPreview()) return
                        val activity = param.thisObject as? Activity ?: return
                        val entry = selectedEntry(param.thisObject)
                        runCatching {
                            LenovoLockVideoWallpaperManager.activateImportedVideo(activity, entry?.id)
                        }.onSuccess {
                            Toast.makeText(activity, "ZUXOS+ lock video wallpaper applied", Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            Toast.makeText(
                                activity,
                                "ZUXOS+ lock video wallpaper failed: ${error.message.orEmpty()}",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                        dismissProgressDialog(param.thisObject)
                        activity.finish()
                        param.result = null
                    }
                },
            )
        }
    }

    private fun hookApplyDialogMarker(classLoader: ClassLoader?) {
        val previewClass = XposedHelpers.findClassIfExists(
            "com.zui.wallpapersetting.activity.LocalWallpaperPreviewActivity",
            classLoader,
        ) ?: return
        XposedHelpers.findAndHookMethod(
            previewClass,
            "showDialog",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (param.thisObject.isCustomPreview()) {
                        applyDialogPreview.set(param.thisObject)
                    }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    applyDialogPreview.remove()
                }
            },
        )
    }

    private fun hookApplyDialogOption(classLoader: ClassLoader?) {
        val builderClass = XposedHelpers.findClassIfExists("zui.app.ActionDialog\$Builder", classLoader)
            ?: return
        XposedHelpers.findAndHookMethod(
            builderClass,
            "setOptionItems",
            Int::class.javaPrimitiveType,
            DialogInterface.OnClickListener::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val preview = applyDialogPreview.get() as? Context ?: return
                    if (!preview.isCustomPreview()) return
                    val resId = param.args.getOrNull(0) as? Int ?: return

                    val originalItems = preview.resources.getTextArray(resId)
                    val dynamicIndex = 2
                    val items = originalItems.toMutableList().apply {
                        add(dynamicIndex, "设为桌面动态壁纸（循环播放）")
                    }.toTypedArray()
                    val originalListener = param.args.getOrNull(1) as? DialogInterface.OnClickListener
                        ?: return
                    val wrapped = DialogInterface.OnClickListener { dialog, which ->
                        if (which == dynamicIndex) {
                            applyDesktopLiveWallpaper(preview, dialog)
                        } else {
                            originalListener.onClick(
                                dialog,
                                if (which > dynamicIndex) which - 1 else which,
                            )
                        }
                    }
                    param.result = XposedHelpers.callMethod(
                        param.thisObject,
                        "setOptionItems",
                        items,
                        wrapped,
                    )
                }
            },
        )
    }

    private fun applyDesktopLiveWallpaper(
        context: Context,
        dialog: DialogInterface,
        finishOnDirect: Boolean = true,
    ) {
        val activity = context as? Activity
        val entry = selectedEntry(context)
        runCatching {
            LenovoLockVideoWallpaperManager.activateDesktopLiveWallpaper(context, entry?.id)
        }.onSuccess { method ->
            val message = when (method) {
                LenovoLockVideoWallpaperManager.DesktopApplyMethod.DIRECT ->
                    "ZUXOS+ desktop live wallpaper applied"
                LenovoLockVideoWallpaperManager.DesktopApplyMethod.ROOT ->
                    "ZUXOS+ desktop live wallpaper applied with root"
                LenovoLockVideoWallpaperManager.DesktopApplyMethod.CONFIRMATION_INTENT ->
                    "请选择 ZUXOS+ 视频动态壁纸并确认"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            if (
                finishOnDirect &&
                method != LenovoLockVideoWallpaperManager.DesktopApplyMethod.CONFIRMATION_INTENT
            ) {
                activity?.finish()
            }
        }.onFailure { error ->
            Toast.makeText(
                context,
                "ZUXOS+ desktop live wallpaper failed: ${error.message.orEmpty()}",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun dismissProgressDialog(instance: Any) {
        runCatching {
            val dialog = XposedHelpers.getObjectField(instance, "mProgressDialog") as? android.app.Dialog
            dialog?.dismiss()
        }
    }

    private fun Any?.isCustomPreview(): Boolean {
        val item = runCatching { XposedHelpers.getObjectField(this, "mWallPaperItem") }.getOrNull()
            ?: return false
        return item.isCustomWallpaperItem()
    }

    private fun Any?.isCustomLocalPic(): Boolean {
        val packageName = runCatching { XposedHelpers.callMethod(this, "getPackageName") as? String }.getOrNull()
        val name = customLocalPicId()
        return packageName == LenovoLockVideoWallpaperManager.CUSTOM_ITEM_PACKAGE ||
            LenovoLockVideoWallpaperManager.isCustomItemName(name)
    }

    private fun Any?.isCustomWallpaperItem(): Boolean {
        val packageName = runCatching { XposedHelpers.callMethod(this, "getmPackageName") as? String }.getOrNull()
        val path = runCatching { XposedHelpers.callMethod(this, "getmPath") as? String }.getOrNull()
        return packageName == LenovoLockVideoWallpaperManager.CUSTOM_ITEM_PACKAGE ||
            LenovoLockVideoWallpaperManager.isCustomItemName(path)
    }

    private fun Any?.customLocalPicId(): String? =
        runCatching { XposedHelpers.callMethod(this, "getName") as? String }.getOrNull()

    private fun Any?.isDynamicWallpaperAdapter(): Boolean =
        runCatching { XposedHelpers.getIntField(this, "type") == 2 }.getOrDefault(false)

    private fun selectedEntry(instance: Any?): LenovoLockVideoWallpaperManager.LibraryEntry? {
        val item = runCatching { XposedHelpers.getObjectField(instance, "mWallPaperItem") }.getOrNull()
        val path = runCatching { XposedHelpers.callMethod(item, "getmPath") as? String }.getOrNull()
        return LenovoLockVideoWallpaperManager.entryForItemName(path)
    }

    private fun decodePreview(path: String): Bitmap =
        BitmapFactory.decodeFile(path) ?: Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLACK)
        }
}
