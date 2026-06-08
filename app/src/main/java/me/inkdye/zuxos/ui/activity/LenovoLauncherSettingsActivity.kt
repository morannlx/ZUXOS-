package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RestartScope
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.GlassSwitch
import me.inkdye.zuxos.ui.components.LiquidGlassCardBackdropProvider
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.RestartScopeCard
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.root.rememberRootUiState
import me.inkdye.zuxos.ui.settings.IntSettingSpec
import me.inkdye.zuxos.ui.settings.LenovoLauncherFeatureSettings
import me.inkdye.zuxos.ui.settings.LenovoLauncherGridIntSettings
import me.inkdye.zuxos.ui.settings.LenovoLauncherTextSettings
import me.inkdye.zuxos.ui.settings.SettingKeys
import me.inkdye.zuxos.ui.settings.TextSettingSpec
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberBooleanSettingState
import me.inkdye.zuxos.ui.settings.rememberIntSettingState
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class LenovoLauncherSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LenovoLauncherSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun LenovoLauncherSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    val coroutineScope = rememberCoroutineScope()
    val rootState = rememberRootUiState()
    var restartMessage by remember { mutableStateOf<String?>(null) }
    var restartFailed by remember { mutableStateOf(false) }

    val hotseatSpec = LenovoLauncherFeatureSettings.first { it.key == SettingKeys.LAUNCHER_HOTSEAT_EXPAND }
    val gridSpec = LenovoLauncherFeatureSettings.first { it.key == SettingKeys.LAUNCHER_CUSTOM_GRID }
    val forceStopSpec = LenovoLauncherFeatureSettings.first { it.key == SettingKeys.LAUNCHER_DISABLE_FORCE_STOP }
    val whitelistModeSpec = LenovoLauncherFeatureSettings.first { it.key == SettingKeys.LAUNCHER_FORCE_STOP_WHITELIST_ENABLE }
    val memorySpec = LenovoLauncherFeatureSettings.first { it.key == SettingKeys.LAUNCHER_RECENTS_MEMORY }
    val hideIconLabelSpec = LenovoLauncherFeatureSettings.first { it.key == SettingKeys.LAUNCHER_HIDE_ICON_LABEL }
    val gridState = rememberBooleanSettingState(repository, gridSpec)
    val forceStopState = rememberBooleanSettingState(repository, forceStopSpec)
    val whitelistModeState = rememberBooleanSettingState(repository, whitelistModeSpec)

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
                columns = GridCells.Adaptive(minSize = 260.dp),
                modifier = Modifier.fillMaxSize().safeContentPadding().padding(horizontal = horizontalPadding),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PageHeader(
                        title = stringResource(R.string.lenovo_launcher_settings_title),
                        subtitle = stringResource(R.string.lenovo_launcher_settings_desc),
                        colorScheme = cs,
                        onBack = onBack,
                    )
                }

                item {
                    val state = rememberBooleanSettingState(repository, hotseatSpec)
                    GlassSwitch(
                        title = stringResource(hotseatSpec.titleRes),
                        description = stringResource(hotseatSpec.descRes),
                        checked = state.checked.value,
                        onCheckedChange = { state.update(it) },
                        colorScheme = cs,
                    )
                }
                item {
                    GlassSwitch(
                        title = stringResource(gridSpec.titleRes),
                        description = stringResource(gridSpec.descRes),
                        checked = gridState.checked.value,
                        onCheckedChange = { gridState.update(it) },
                        colorScheme = cs,
                    )
                }
                if (gridState.checked.value) {
                    LenovoLauncherGridIntSettings.forEach { spec ->
                        item {
                            IntSettingCard(spec = spec, colorScheme = cs)
                        }
                    }
                }
                item {
                    GlassSwitch(
                        title = stringResource(forceStopSpec.titleRes),
                        description = stringResource(forceStopSpec.descRes),
                        checked = forceStopState.checked.value,
                        onCheckedChange = { forceStopState.update(it) },
                        colorScheme = cs,
                    )
                }
                if (forceStopState.checked.value) {
                    item {
                        GlassSwitch(
                            title = stringResource(whitelistModeSpec.titleRes),
                            description = stringResource(whitelistModeSpec.descRes),
                            checked = whitelistModeState.checked.value,
                            onCheckedChange = { whitelistModeState.update(it) },
                            colorScheme = cs,
                        )
                    }
                    if (whitelistModeState.checked.value) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            TextSettingCard(
                                spec = LenovoLauncherTextSettings.first(),
                                placeholder = stringResource(R.string.lenovo_launcher_whitelist_placeholder),
                                colorScheme = cs,
                            )
                        }
                    }
                }
                item {
                    val state = rememberBooleanSettingState(repository, memorySpec)
                    GlassSwitch(
                        title = stringResource(memorySpec.titleRes),
                        description = stringResource(memorySpec.descRes),
                        checked = state.checked.value,
                        onCheckedChange = { state.update(it) },
                        colorScheme = cs,
                    )
                }
                item {
                    val state = rememberBooleanSettingState(repository, hideIconLabelSpec)
                    GlassSwitch(
                        title = stringResource(hideIconLabelSpec.titleRes),
                        description = stringResource(hideIconLabelSpec.descRes),
                        checked = state.checked.value,
                        onCheckedChange = { state.update(it) },
                        colorScheme = cs,
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    RestartScopeCard(
                        scope = RestartScope.ZUX_LAUNCHER,
                        colorScheme = cs,
                        onRestart = {
                            rootState.restart(coroutineScope, RestartScope.ZUX_LAUNCHER) { result ->
                                restartMessage = result.message
                                restartFailed = !result.success
                            }
                        },
                    )
                }
                restartMessage?.let { message ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = if (restartFailed) cs.accentRed else cs.accentGreen,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntSettingCard(spec: IntSettingSpec, colorScheme: AppColorScheme) {
    val repository = rememberSettingsRepository()
    val state = rememberIntSettingState(repository, spec)
    var text by remember { mutableStateOf(state.value.value.toString()) }

    LaunchedEffect(state.value.value) {
        text = state.value.value.toString()
    }

    GlassCard(colorScheme = colorScheme) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(spec.titleRes),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colorScheme.textPrimary,
            )
            Text(
                text = stringResource(spec.descRes),
                fontSize = 12.sp,
                color = colorScheme.textDim,
            )
            OutlinedTextField(
                value = text,
                onValueChange = { next ->
                    val digits = next.filter { it.isDigit() }
                    text = digits
                    digits.toIntOrNull()?.let { state.update(it) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TextSettingCard(
    spec: TextSettingSpec,
    placeholder: String,
    colorScheme: AppColorScheme,
) {
    val repository = rememberSettingsRepository()
    var text by remember(spec.key) {
        mutableStateOf(repository.getText(spec))
    }

    GlassCard(colorScheme = colorScheme) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(spec.titleRes),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colorScheme.textPrimary,
                )
            }
            Text(
                text = stringResource(spec.descRes),
                fontSize = 12.sp,
                color = colorScheme.textDim,
            )
            OutlinedTextField(
                value = text,
                onValueChange = { next ->
                    text = next
                    repository.setText(spec, next)
                },
                placeholder = { Text(placeholder) },
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
