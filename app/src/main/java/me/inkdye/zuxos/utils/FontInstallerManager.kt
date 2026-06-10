package me.inkdye.zuxos.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.text.TextUtils
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RootManager
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.random.Random

object FontInstallerManager {
    private const val FONT_BASE_PATH =
        "/data_mirror/data_ce/null/0/com.zui.homesettings/files/.ZFont/.localFont"
    private const val TEMP_FONT_DIR = "temp_fonts"

    fun copyFontToTemp(context: Context, uri: Uri): File {
        val tempDir = File(context.filesDir, TEMP_FONT_DIR).apply { mkdirs() }
        val tempFile = File(tempDir, "temp_font_${System.currentTimeMillis()}.ttf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        } ?: error("openInputStream returned null")
        return tempFile
    }

    suspend fun installFont(
        context: Context,
        fontFile: File,
        fontName: String,
        fontDescription: String,
    ) {
        require(fontFile.isFile) { "Font file does not exist" }
        val folderName = generateRandomFolderName()
        val targetFolderPath = "$FONT_BASE_PATH/$folderName"
        val tempDir = File(context.filesDir, TEMP_FONT_DIR).apply { mkdirs() }

        try {
            runRoot("mkdir -p ${shellQuote(targetFolderPath)}")
            copyFileWithRoot(fontFile, "$targetFolderPath/font.ttf")

            val xmlFile = File(tempDir, "font.xml")
            xmlFile.writeText(generateFontXml(context, fontName, fontDescription))
            copyFileWithRoot(xmlFile, "$targetFolderPath/font.xml")

            generatePreviewImages(context, tempDir, targetFolderPath, fontFile, fontName)
            setFolderPermissions(targetFolderPath)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private suspend fun generatePreviewImages(
        context: Context,
        tempDir: File,
        targetFolderPath: String,
        fontFile: File,
        fontName: String,
    ) {
        val typeface = Typeface.createFromFile(fontFile)
        val small = generateFontPreviewBitmap(typeface, fontName, width = 249, height = 70, maxTextSize = 42f)
        val smallFile = saveBitmapToTemp(tempDir, small, "small.png")
        copyFileWithRoot(smallFile, "$targetFolderPath/small.png")
        small.recycle()

        val preview = generateFontPreviewBitmap(
            typeface = typeface,
            text = context.getString(R.string.font_preview_text),
            width = 948,
            height = 945,
            maxTextSize = 52f,
        )
        val previewFile = saveBitmapToTemp(tempDir, preview, "preview.png")
        copyFileWithRoot(previewFile, "$targetFolderPath/preview.png")
        preview.recycle()
    }

    private fun generateFontPreviewBitmap(
        typeface: Typeface,
        text: String,
        width: Int,
        height: Int,
        maxTextSize: Float,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        val lines = text.split('\n')
        val textSize = calculateTextSize(paint, lines, width * 0.86f, height * 0.86f, maxTextSize)
        paint.textSize = textSize
        val fontMetrics = paint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * 1.22f
        val contentHeight = lineHeight * lines.size
        var baseline = (height - contentHeight) / 2f - fontMetrics.ascent
        lines.forEach { line ->
            canvas.drawText(line, width / 2f, baseline, paint)
            baseline += lineHeight
        }
        return bitmap
    }

    private fun calculateTextSize(
        paint: Paint,
        lines: List<String>,
        maxWidth: Float,
        maxHeight: Float,
        maxTextSize: Float,
    ): Float {
        var size = maxTextSize
        while (size > 12f) {
            paint.textSize = size
            val fontMetrics = paint.fontMetrics
            val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * 1.22f
            val widest = lines.maxOfOrNull { paint.measureText(it) } ?: 0f
            if (widest <= maxWidth && lineHeight * lines.size <= maxHeight) return size
            size -= min(2f, size / 10f)
        }
        return size
    }

    private suspend fun setFolderPermissions(folderPath: String) {
        val stat = RootManager.execute("ls -ld ${shellQuote(FONT_BASE_PATH)}")
        if (!stat.success) return
        val parts = stat.output.split(Regex("\\s+"))
        if (parts.size < 4) return
        val owner = parts[2]
        val group = parts[3]
        runRoot("chown -R ${shellQuote("$owner:$group")} ${shellQuote(folderPath)}")
        runRoot("chmod 700 ${shellQuote(folderPath)}")
        runRoot("chmod 600 ${shellQuote(folderPath)}/*")
    }

    private suspend fun copyFileWithRoot(source: File, dest: String) {
        runRoot("cp ${shellQuote(source.absolutePath)} ${shellQuote(dest)}")
    }

    private fun saveBitmapToTemp(tempDir: File, bitmap: Bitmap, name: String): File {
        val file = File(tempDir, name)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return file
    }

    private suspend fun runRoot(command: String) {
        val result = RootManager.execute(command)
        if (!result.success) error(result.message)
    }

    private fun generateRandomFolderName(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz"
        return buildString {
            repeat(6) { append(chars[Random.nextInt(chars.length)]) }
        }
    }

    private fun generateFontXml(context: Context, name: String, desc: String): String {
        val safeName = TextUtils.htmlEncode(name)
        val safeDesc = TextUtils.htmlEncode(desc)
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <ZFont>
            <name>$safeName</name>
            <language>${context.getString(R.string.font_language)}</language>
            <author>${context.getString(R.string.font_author)}</author>
            <abstract>$safeDesc</abstract>
            </ZFont>
        """.trimIndent()
    }

    private fun shellQuote(value: String): String = "'${value.replace("'", "'\"'\"'")}'"
}
