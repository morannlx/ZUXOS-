package me.inkdye.zuxos.ui.activity

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.provider.AboutDeviceImageProvider
import me.inkdye.zuxos.root.RestartScope
import me.inkdye.zuxos.ui.components.BooleanSettingSwitch
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.GlassSwitch
import me.inkdye.zuxos.ui.components.LiquidGlassCardBackdropProvider
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.RestartScopeCard
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.root.rememberRootUiState
import me.inkdye.zuxos.ui.settings.LenovoAboutDeviceImageSetting
import me.inkdye.zuxos.ui.settings.LenovoAboutDeviceMasterSetting
import me.inkdye.zuxos.ui.settings.LenovoAboutDeviceTextSettings
import me.inkdye.zuxos.ui.settings.SettingsRepository
import me.inkdye.zuxos.ui.settings.TextSettingSpec
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark
import java.io.File
import java.io.FileOutputStream

class LenovoAboutDeviceSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LenovoAboutDeviceSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun LenovoAboutDeviceSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val context = LocalContext.current
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    val coroutineScope = rememberCoroutineScope()
    val rootState = rememberRootUiState()
    var imageVersion by remember { mutableIntStateOf(0) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusFailed by remember { mutableStateOf(false) }
    var imageReplacementEnabled by remember {
        mutableStateOf(repository.getBoolean(LenovoAboutDeviceImageSetting))
    }

    fun saveSelectedImage(uri: Uri) {
        runCatching {
            val outputFile = File(context.filesDir, AboutDeviceImageProvider.FILE_NAME)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outputFile).use { output -> input.copyTo(output) }
            } ?: error("openInputStream returned null")
            repository.setString(
                ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH,
                AboutDeviceImageProvider.IMAGE_URI.toString(),
            )
            imageVersion++
            statusFailed = false
            statusMessage = context.getString(R.string.lenovo_about_device_image_saved)
        }.onFailure { error ->
            statusFailed = true
            statusMessage = context.getString(R.string.lenovo_about_device_image_save_failed, error.message.orEmpty())
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) saveSelectedImage(uri)
    }

    val bgBrush = Brush.verticalGradient(
        listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)
    )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bgBrush)
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
                        title = stringResource(R.string.lenovo_about_device_settings_title),
                        subtitle = stringResource(R.string.lenovo_about_device_settings_desc),
                        colorScheme = cs,
                        onBack = onBack,
                    )
                }

                item {
                    BooleanSettingSwitch(
                        spec = LenovoAboutDeviceMasterSetting,
                        repository = repository,
                        colorScheme = cs,
                    )
                }

                item {
                    GlassSwitch(
                        title = stringResource(LenovoAboutDeviceImageSetting.titleRes),
                        description = stringResource(LenovoAboutDeviceImageSetting.descRes),
                        checked = imageReplacementEnabled,
                        onCheckedChange = { enabled ->
                            imageReplacementEnabled = enabled
                            repository.setBoolean(LenovoAboutDeviceImageSetting, enabled)
                        },
                        colorScheme = cs,
                    )
                }

                if (imageReplacementEnabled) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        DeviceImageCard(
                            repository = repository,
                            colorScheme = cs,
                            imageVersion = imageVersion,
                            onSelectImage = { imagePicker.launch("image/*") },
                            onClearImage = {
                                repository.setString(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH, "")
                                File(context.filesDir, AboutDeviceImageProvider.FILE_NAME).delete()
                                imageVersion++
                            },
                        )
                    }
                }

                items(LenovoAboutDeviceTextSettings) { spec ->
                    DeviceInfoTextCard(
                        spec = spec,
                        repository = repository,
                        colorScheme = cs,
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    RestartScopeCard(
                        scope = RestartScope.SETTINGS,
                        colorScheme = cs,
                        onRestart = {
                            rootState.restart(coroutineScope, RestartScope.SETTINGS) { result ->
                                statusMessage = result.message
                                statusFailed = !result.success
                            }
                        },
                    )
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
}

@Composable
private fun DeviceImageCard(
    repository: SettingsRepository,
    colorScheme: AppColorScheme,
    imageVersion: Int,
    onSelectImage: () -> Unit,
    onClearImage: () -> Unit,
) {
    val context = LocalContext.current
    val path = repository.getString(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH)
    val preview = remember(path, imageVersion) {
        File(context.filesDir, AboutDeviceImageProvider.FILE_NAME)
            .takeIf { it.isFile }
            ?.let { BitmapFactory.decodeFile(it.absolutePath) }
    }

    GlassCard(colorScheme = colorScheme) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 150.dp, height = 90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorScheme.glassSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    if (preview != null) {
                        Image(
                            bitmap = preview.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.lenovo_about_device_image_empty),
                            fontSize = 12.sp,
                            color = colorScheme.textDim,
                        )
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.lenovo_about_device_image_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = path.ifBlank { stringResource(R.string.lenovo_about_device_image_path_empty) },
                        fontSize = 12.sp,
                        color = colorScheme.textDim,
                        lineHeight = 17.sp,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onSelectImage) {
                    Text(stringResource(R.string.lenovo_about_device_select_image))
                }
                TextButton(onClick = onClearImage) {
                    Text(stringResource(R.string.lenovo_about_device_clear_image))
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoTextCard(
    spec: TextSettingSpec,
    repository: SettingsRepository,
    colorScheme: AppColorScheme,
) {
    var value by remember(spec.key) { mutableStateOf(repository.getText(spec)) }
    var editing by remember { mutableStateOf(false) }
    val displayValue = value.ifBlank { stringResource(R.string.lenovo_about_device_keep_original) }

    GlassCard(
        colorScheme = colorScheme,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) { editing = true },
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(spec.titleRes),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colorScheme.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(spec.descRes),
                fontSize = 12.sp,
                color = colorScheme.textDim,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = displayValue,
                fontSize = 13.sp,
                color = if (value.isBlank()) colorScheme.textSecondary else colorScheme.accentBlue,
            )
        }
    }

    if (editing) {
        EditTextSettingDialog(
            title = stringResource(spec.titleRes),
            initialValue = value,
            placeholder = spec.defaultValue,
            colorScheme = colorScheme,
            onDismiss = { editing = false },
            onSave = { next ->
                value = next.trim()
                repository.setText(spec, value)
                editing = false
            },
            onClear = {
                value = ""
                repository.setText(spec, "")
                editing = false
            },
        )
    }
}

@Composable
private fun EditTextSettingDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    colorScheme: AppColorScheme,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
) {
    var text by remember(initialValue) { mutableStateOf(initialValue.ifBlank { placeholder }) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text(
                        text = stringResource(R.string.clear),
                        color = colorScheme.accentRed,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
    )
}
