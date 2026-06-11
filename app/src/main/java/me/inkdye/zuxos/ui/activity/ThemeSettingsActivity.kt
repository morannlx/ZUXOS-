package me.inkdye.zuxos.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RootManager
import me.inkdye.zuxos.ui.components.ActionCard
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.LiquidGlassCardBackdropProvider
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark
import me.inkdye.zuxos.utils.FontInstallerManager
import me.inkdye.zuxos.wallpaper.LenovoLockVideoWallpaperManager
import java.io.File

class ThemeSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun ThemeSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    val coroutineScope = rememberCoroutineScope()
    var selectedFont by remember { mutableStateOf<File?>(null) }
    var originalFileName by remember { mutableStateOf("") }
    var showInputDialog by remember { mutableStateOf(false) }
    var busyMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusFailed by remember { mutableStateOf(false) }

    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            busyMessage = context.getString(R.string.preparing_font_file)
            statusMessage = null
            runCatching {
                withContext(Dispatchers.IO) {
                    FontInstallerManager.copyFontToTemp(context, uri)
                }
            }.onSuccess { file ->
                selectedFont = file
                originalFileName = context.displayName(uri) ?: context.getString(R.string.unknown_font_filename)
                busyMessage = null
                showInputDialog = true
            }.onFailure { error ->
                busyMessage = null
                statusFailed = true
                statusMessage = context.getString(R.string.font_file_error, error.message.orEmpty())
            }
        }
    }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            busyMessage = context.getString(R.string.lock_video_wallpaper_importing)
            statusMessage = null
            runCatching {
                withContext(Dispatchers.IO) {
                    uris.map { uri ->
                        LenovoLockVideoWallpaperManager.importVideo(context, uri)
                    }
                }
            }.onSuccess { results ->
                busyMessage = null
                statusFailed = false
                statusMessage = if (results.size == 1) {
                    val result = results.first()
                    when (result.method) {
                        LenovoLockVideoWallpaperManager.ApplyMethod.DIRECT ->
                            context.getString(R.string.lock_video_wallpaper_import_success_direct, result.videoPath)
                        LenovoLockVideoWallpaperManager.ApplyMethod.ROOT ->
                            context.getString(R.string.lock_video_wallpaper_import_success_root, result.videoPath)
                    }
                } else {
                    context.getString(R.string.lock_video_wallpaper_import_success_multi, results.size)
                }
            }.onFailure { error ->
                busyMessage = null
                statusFailed = true
                statusMessage = context.getString(
                    R.string.lock_video_wallpaper_import_failed,
                    error.message.orEmpty(),
                )
            }
        }
    }

    val bgBrush = Brush.verticalGradient(listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom))

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bgBrush),
    ) {
        LiquidGlassCardBackdropProvider(
            backdropBrush = bgBrush,
            enabled = settings.liquidGlassEnabled.value,
            colorScheme = cs,
        ) {
            val horizontalPadding = pageHorizontalPadding()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                modifier = Modifier.fillMaxSize().safeContentPadding().padding(horizontal = horizontalPadding),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PageHeader(
                        title = stringResource(R.string.theme_settings_title),
                        subtitle = stringResource(R.string.theme_settings_desc),
                        colorScheme = cs,
                        onBack = onBack,
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ActionCard(
                        title = stringResource(R.string.import_font_title),
                        description = stringResource(R.string.import_font_summary),
                        colorScheme = cs,
                        onClick = {
                            fontPicker.launch(
                                arrayOf(
                                    "font/ttf",
                                    "application/x-font-ttf",
                                    "application/octet-stream",
                                    "*/*",
                                ),
                            )
                        },
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ActionCard(
                        title = stringResource(R.string.lock_video_wallpaper_title),
                        description = stringResource(R.string.lock_video_wallpaper_summary),
                        colorScheme = cs,
                        onClick = {
                            videoPicker.launch(arrayOf("video/mp4", "video/*"))
                        },
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ActionCard(
                        title = stringResource(R.string.lock_video_wallpaper_library_title),
                        description = stringResource(R.string.lock_video_wallpaper_library_summary),
                        colorScheme = cs,
                        onClick = {
                            coroutineScope.launch {
                                busyMessage = context.getString(R.string.lock_video_wallpaper_library_opening)
                                statusMessage = null
                                runCatching {
                                    context.killLenovoWallpaperLibrary()
                                    context.openLenovoWallpaperLibrary()
                                }.onSuccess {
                                    busyMessage = null
                                    statusFailed = false
                                    statusMessage = context.getString(R.string.lock_video_wallpaper_library_opened)
                                }.onFailure { error ->
                                    busyMessage = null
                                    statusFailed = true
                                    statusMessage = context.getString(
                                        R.string.lock_video_wallpaper_library_open_failed,
                                        error.message.orEmpty(),
                                    )
                                }
                            }
                        },
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ActionCard(
                            title = stringResource(R.string.lock_video_wallpaper_file_access_title),
                            description = stringResource(R.string.lock_video_wallpaper_file_access_desc),
                            colorScheme = cs,
                            onClick = {
                                context.openAllFilesAccessSettings()
                            },
                        )
                    }
                }

                busyMessage?.let { message ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ImportStatusCard(message = message, colorScheme = cs, showProgress = true)
                    }
                }

                statusMessage?.let { message ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = if (statusFailed) cs.accentRed else cs.accentGreen,
                        )
                    }
                }
            }
        }
    }

    if (showInputDialog) {
        FontInputDialog(
            originalFileName = originalFileName,
            colorScheme = cs,
            onDismiss = { showInputDialog = false },
            onImport = { name, description ->
                val fontFile = selectedFont ?: return@FontInputDialog
                showInputDialog = false
                coroutineScope.launch {
                    busyMessage = context.getString(R.string.importing_font)
                    statusMessage = null
                    runCatching {
                        withContext(Dispatchers.IO) {
                            FontInstallerManager.installFont(context, fontFile, name, description)
                        }
                    }.onSuccess {
                        busyMessage = null
                        statusFailed = false
                        statusMessage = context.getString(R.string.import_success_message)
                    }.onFailure { error ->
                        busyMessage = null
                        statusFailed = true
                        statusMessage = context.getString(R.string.import_failed_message, error.message.orEmpty())
                    }
                }
            },
        )
    }
}

@Composable
private fun ImportStatusCard(message: String, colorScheme: AppColorScheme, showProgress: Boolean) {
    GlassCard(colorScheme = colorScheme) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showProgress) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 2.dp))
            }
            Text(message, fontSize = 13.sp, color = colorScheme.textPrimary)
        }
    }
}

@Composable
private fun FontInputDialog(
    originalFileName: String,
    colorScheme: AppColorScheme,
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit,
) {
    val defaultDescription = stringResource(R.string.default_font_description, originalFileName)
    var fontName by remember(originalFileName) { mutableStateOf(originalFileName.substringBeforeLast('.')) }
    var fontDescription by remember(originalFileName) {
        mutableStateOf(defaultDescription)
    }
    val nameEmpty = stringResource(R.string.font_name_empty)
    val descEmpty = stringResource(R.string.font_description_empty)
    var validationMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.input_font_info_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.do_not_use_duplicated_font_name),
                    fontSize = 12.sp,
                    color = colorScheme.accentRed,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = fontName,
                    onValueChange = { fontName = it },
                    label = { Text(stringResource(R.string.font_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = fontDescription,
                    onValueChange = { fontDescription = it },
                    label = { Text(stringResource(R.string.font_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                validationMessage?.let {
                    Text(it, fontSize = 12.sp, color = colorScheme.accentRed)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val name = fontName.trim()
                    val description = fontDescription.ifBlank { defaultDescription }.trim()
                    validationMessage = when {
                        name.isEmpty() -> nameEmpty
                        description.isEmpty() -> descEmpty
                        else -> null
                    }
                    if (validationMessage == null) onImport(name, description)
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun android.content.Context.displayName(uri: Uri): String? {
    val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && it.moveToFirst()) return it.getString(index)
    }
    return uri.lastPathSegment
}

private fun android.content.Context.openAllFilesAccessSettings() {
    val appIntent = Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Uri.parse("package:$packageName"),
    )
    runCatching {
        startActivity(appIntent)
    }.onFailure {
        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
    }
}

private suspend fun android.content.Context.killLenovoWallpaperLibrary() {
    withContext(Dispatchers.IO) {
        runCatching {
            ProcessBuilder(
                "sh",
                "-c",
                "am force-stop ${LenovoLockVideoWallpaperManager.WALLPAPER_SETTING_PACKAGE}",
            ).start().waitFor()
        }
        runCatching {
            RootManager.execute(
                "am force-stop ${LenovoLockVideoWallpaperManager.WALLPAPER_SETTING_PACKAGE}",
                timeoutSeconds = 5,
            )
        }
    }
}

private fun android.content.Context.openLenovoWallpaperLibrary() {
    val explicitIntent = Intent().apply {
        component = ComponentName(
            LenovoLockVideoWallpaperManager.WALLPAPER_SETTING_PACKAGE,
            "com.zui.wallpapersetting.activity.CustomizaActivity",
        )
    }
    runCatching {
        startActivity(explicitIntent)
    }.getOrElse {
        val launcher = packageManager.getLaunchIntentForPackage(
            LenovoLockVideoWallpaperManager.WALLPAPER_SETTING_PACKAGE,
        ) ?: throw it
        startActivity(launcher)
    }
}
