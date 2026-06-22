package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RootManager
import me.inkdye.zuxos.ui.components.AppBackground
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class DisplaySizeSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisplaySizeSettingsPage(onBack = { finish() })
        }
    }
}

private data class DisplaySize(val width: Int, val height: Int) {
    override fun toString(): String = "${width}x$height"
}

private data class DisplayConfig(
    val physicalSize: DisplaySize?,
    val overrideSize: DisplaySize?,
    val physicalDensity: Int?,
    val overrideDensity: Int?,
) {
    val effectiveSize: DisplaySize? get() = overrideSize ?: physicalSize
    val effectiveDensity: Int? get() = overrideDensity ?: physicalDensity

    fun restoreCommand(): String {
        val sizeCommand = overrideSize?.let { "wm size ${it.width}x${it.height}" } ?: "wm size reset"
        val densityCommand = overrideDensity?.let { "wm density $it" } ?: "wm density reset"
        return "$sizeCommand && $densityCommand"
    }
}

private data class DisplayPreset(
    val label: String,
    val size: DisplaySize,
    val density: Int,
)

private data class PendingDisplayChange(
    val id: Long,
    val before: DisplayConfig,
    val summary: String,
)

@Composable
private fun DisplaySizeSettingsPage(onBack: () -> Unit) {
    val appSettings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(appSettings.themeMode.value),
        titleTextContrast = appSettings.titleTextContrast.value,
        descriptionTextContrast = appSettings.descriptionTextContrast.value,
    )
    val coroutineScope = rememberCoroutineScope()
    var config by remember { mutableStateOf<DisplayConfig?>(null) }
    var dpiText by remember { mutableStateOf("") }
    var widthText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var fastApply by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var statusFailed by remember { mutableStateOf(false) }
    var pendingChange by remember { mutableStateOf<PendingDisplayChange?>(null) }
    var countdown by remember { mutableIntStateOf(10) }

    val loadFailed = stringResource(R.string.display_size_load_failed)
    val rootDenied = stringResource(R.string.display_size_root_denied)
    val invalidInput = stringResource(R.string.display_size_invalid_input)
    val applySuccess = stringResource(R.string.display_size_apply_success)
    val applyFailed = stringResource(R.string.display_size_apply_failed)
    val restoreSuccess = stringResource(R.string.display_size_restore_success)
    val restoreFailed = stringResource(R.string.display_size_restore_failed)
    val defaultsSummary = stringResource(R.string.display_size_defaults_summary)

    fun setStatus(message: String, failed: Boolean = false) {
        statusText = message
        statusFailed = failed
    }

    fun fillFromConfig(nextConfig: DisplayConfig?) {
        nextConfig?.effectiveSize?.let { size ->
            widthText = size.width.toString()
            heightText = size.height.toString()
        }
        nextConfig?.effectiveDensity?.let { density ->
            dpiText = density.toString()
        }
    }

    fun refreshConfig() {
        coroutineScope.launch {
            busy = true
            val root = RootManager.requestAccess()
            if (!root.isGranted) {
                busy = false
                setStatus(root.message ?: rootDenied, failed = true)
                return@launch
            }
            val result = RootManager.execute("wm size; wm density")
            busy = false
            if (result.success) {
                val nextConfig = parseDisplayConfig(result.output)
                config = nextConfig
                fillFromConfig(nextConfig)
            } else {
                setStatus(loadFailed.formatForMessage(result.message), failed = true)
            }
        }
    }

    fun restorePending(change: PendingDisplayChange) {
        coroutineScope.launch {
            busy = true
            val result = RootManager.execute(change.before.restoreCommand())
            val refreshed = RootManager.execute("wm size; wm density")
            busy = false
            pendingChange = null
            if (refreshed.success) {
                config = parseDisplayConfig(refreshed.output)
                fillFromConfig(config)
            }
            setStatus(
                if (result.success) restoreSuccess else restoreFailed.formatForMessage(result.message),
                failed = !result.success,
            )
        }
    }

    fun applyCommand(command: String, summary: String) {
        coroutineScope.launch {
            busy = true
            val root = RootManager.requestAccess()
            if (!root.isGranted) {
                busy = false
                setStatus(root.message ?: rootDenied, failed = true)
                return@launch
            }
            val beforeResult = RootManager.execute("wm size; wm density")
            val before = if (beforeResult.success) parseDisplayConfig(beforeResult.output) else config
            if (before == null) {
                busy = false
                setStatus(loadFailed.formatForMessage(beforeResult.message), failed = true)
                return@launch
            }
            val applyResult = RootManager.execute(command)
            val afterResult = RootManager.execute("wm size; wm density")
            busy = false
            if (afterResult.success) {
                config = parseDisplayConfig(afterResult.output)
                fillFromConfig(config)
            }
            if (applyResult.success) {
                pendingChange = PendingDisplayChange(
                    id = System.nanoTime(),
                    before = before,
                    summary = summary,
                )
                setStatus(applySuccess, failed = false)
            } else {
                setStatus(applyFailed.formatForMessage(applyResult.message), failed = true)
            }
        }
    }

    fun applyCustomValues() {
        val dpi = dpiText.toIntOrNull()
        val width = widthText.toIntOrNull()
        val height = heightText.toIntOrNull()
        if (dpi == null || width == null || height == null ||
            dpi !in 120..1000 || width !in 320..10000 || height !in 320..10000
        ) {
            setStatus(invalidInput, failed = true)
            return
        }
        applyCommand(
            command = "wm size ${width}x$height && wm density $dpi",
            summary = "${width}x$height / DPI $dpi",
        )
    }

    fun applyDefaults() {
        applyCommand(
            command = "wm size reset && wm density reset",
            summary = defaultsSummary,
        )
    }

    LaunchedEffect(Unit) {
        refreshConfig()
    }

    LaunchedEffect(pendingChange?.id) {
        val change = pendingChange ?: return@LaunchedEffect
        for (remaining in 10 downTo 1) {
            countdown = remaining
            delay(1000)
            if (pendingChange?.id != change.id) return@LaunchedEffect
        }
        if (pendingChange?.id == change.id) {
            restorePending(change)
        }
    }

    AppBackground(colorScheme = cs) {
        val horizontalPadding = pageHorizontalPadding()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = stringResource(R.string.display_size_settings_title),
                    subtitle = stringResource(R.string.display_size_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            item {
                CurrentDisplayCard(config = config, colorScheme = cs)
            }

            item {
                DisplayEditorCard(
                    dpiText = dpiText,
                    widthText = widthText,
                    heightText = heightText,
                    fastApply = fastApply,
                    busy = busy,
                    config = config,
                    colorScheme = cs,
                    onDpiChange = { dpiText = it.filter(Char::isDigit).take(4) },
                    onWidthChange = { widthText = it.filter(Char::isDigit).take(5) },
                    onHeightChange = { heightText = it.filter(Char::isDigit).take(5) },
                    onFastApplyChange = { fastApply = it },
                    onPreset = { preset ->
                        widthText = preset.size.width.toString()
                        heightText = preset.size.height.toString()
                        dpiText = preset.density.toString()
                    },
                    onDefaults = {
                        if (fastApply) {
                            applyDefaults()
                        } else {
                            config?.physicalSize?.let {
                                widthText = it.width.toString()
                                heightText = it.height.toString()
                            }
                            config?.physicalDensity?.let { dpiText = it.toString() }
                        }
                    },
                    onApply = { applyCustomValues() },
                    onRefresh = { refreshConfig() },
                )
            }

            statusText?.let { message ->
                item {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = if (statusFailed) cs.accentRed else cs.accentGreen,
                    )
                }
            }
        }
    }

    pendingChange?.let { change ->
        ConfirmDisplayChangeDialog(
            change = change,
            countdown = countdown,
            colorScheme = cs,
            onConfirm = {
                pendingChange = null
                setStatus(applySuccess, failed = false)
            },
            onRestore = { restorePending(change) },
        )
    }
}

@Composable
private fun CurrentDisplayCard(config: DisplayConfig?, colorScheme: AppColorScheme) {
    GlassCard(colorScheme = colorScheme) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.display_size_current_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.textPrimary,
            )
            Text(
                text = config?.effectiveSize?.let {
                    stringResource(R.string.display_size_current_size, it.width, it.height)
                } ?: stringResource(R.string.display_size_unknown),
                fontSize = 13.sp,
                color = colorScheme.textSecondary,
            )
            Text(
                text = config?.effectiveDensity?.let {
                    stringResource(R.string.display_size_current_density, it)
                } ?: stringResource(R.string.display_size_unknown),
                fontSize = 13.sp,
                color = colorScheme.textSecondary,
            )
            Text(
                text = stringResource(
                    R.string.display_size_original_values,
                    config?.physicalSize?.toString() ?: "-",
                    config?.physicalDensity?.toString() ?: "-",
                ),
                fontSize = 12.sp,
                color = colorScheme.textDim,
            )
        }
    }
}

@Composable
private fun DisplayEditorCard(
    dpiText: String,
    widthText: String,
    heightText: String,
    fastApply: Boolean,
    busy: Boolean,
    config: DisplayConfig?,
    colorScheme: AppColorScheme,
    onDpiChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onFastApplyChange: (Boolean) -> Unit,
    onPreset: (DisplayPreset) -> Unit,
    onDefaults: () -> Unit,
    onApply: () -> Unit,
    onRefresh: () -> Unit,
) {
    val presets = listOf(
        DisplayPreset("HD", DisplaySize(1280, 720), 320),
        DisplayPreset("FHD", DisplaySize(1920, 1080), 420),
        DisplayPreset("2K", DisplaySize(2560, 1440), 480),
        DisplayPreset("4K", DisplaySize(3840, 2160), 640),
    )

    GlassCard(colorScheme = colorScheme, shape = RoundedCornerShape(20.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.display_size_editor_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary,
            )
            WarningBox(colorScheme = colorScheme)
            OutlinedTextField(
                value = dpiText,
                onValueChange = onDpiChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.display_size_dpi_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = widthText,
                    onValueChange = onWidthChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.display_size_width_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Text(
                    text = "x",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontSize = 18.sp,
                    color = colorScheme.textDim,
                )
                OutlinedTextField(
                    value = heightText,
                    onValueChange = onHeightChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.display_size_height_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = fastApply,
                    onCheckedChange = onFastApplyChange,
                )
                Text(
                    text = stringResource(R.string.display_size_fast_apply),
                    fontSize = 14.sp,
                    color = colorScheme.textPrimary,
                )
            }
            Text(
                text = stringResource(R.string.display_size_presets),
                fontSize = 12.sp,
                color = colorScheme.textDim,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                presets.forEach { preset ->
                    Button(
                        enabled = !busy,
                        onClick = { onPreset(preset) },
                    ) {
                        Text(preset.label)
                    }
                }
                Button(
                    enabled = !busy && (config?.physicalSize != null || config?.physicalDensity != null),
                    onClick = onDefaults,
                ) {
                    Text(stringResource(R.string.display_size_reset_preset))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    enabled = !busy,
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.display_size_refresh))
                }
                Button(
                    enabled = !busy,
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.display_size_apply))
                }
            }
        }
    }
}

@Composable
private fun WarningBox(colorScheme: AppColorScheme) {
    GlassCard(
        colorScheme = colorScheme,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = stringResource(R.string.display_size_warning),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = colorScheme.accentRed,
        )
    }
}

@Composable
private fun ConfirmDisplayChangeDialog(
    change: PendingDisplayChange,
    countdown: Int,
    colorScheme: AppColorScheme,
    onConfirm: () -> Unit,
    onRestore: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onRestore,
        title = { Text(stringResource(R.string.display_size_confirm_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.display_size_confirm_desc, change.summary, countdown),
                    color = colorScheme.textSecondary,
                )
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { countdown / 10f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.display_size_confirm_button, countdown))
            }
        },
        dismissButton = {
            TextButton(onClick = onRestore) {
                Text(stringResource(R.string.display_size_restore_now))
            }
        },
    )
}

private fun parseDisplayConfig(output: String): DisplayConfig {
    var physicalSize: DisplaySize? = null
    var overrideSize: DisplaySize? = null
    var physicalDensity: Int? = null
    var overrideDensity: Int? = null
    output.lineSequence().forEach { rawLine ->
        val line = rawLine.trim()
        when {
            line.startsWith("Physical size:") -> {
                physicalSize = line.substringAfter(":").trim().parseDisplaySize()
            }

            line.startsWith("Override size:") -> {
                overrideSize = line.substringAfter(":").trim().parseDisplaySize()
            }

            line.startsWith("Physical density:") -> {
                physicalDensity = line.substringAfter(":").trim().toIntOrNull()
            }

            line.startsWith("Override density:") -> {
                overrideDensity = line.substringAfter(":").trim().toIntOrNull()
            }
        }
    }
    return DisplayConfig(
        physicalSize = physicalSize,
        overrideSize = overrideSize,
        physicalDensity = physicalDensity,
        overrideDensity = overrideDensity,
    )
}

private fun String.parseDisplaySize(): DisplaySize? {
    val parts = lowercase().split("x", limit = 2)
    if (parts.size != 2) return null
    val width = parts[0].trim().toIntOrNull() ?: return null
    val height = parts[1].trim().toIntOrNull() ?: return null
    return DisplaySize(width, height)
}

private fun String.formatForMessage(detail: String): String = format(detail.ifBlank { "unknown" })
