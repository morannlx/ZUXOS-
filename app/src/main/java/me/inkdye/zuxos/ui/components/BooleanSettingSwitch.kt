package me.inkdye.zuxos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.inkdye.zuxos.ui.settings.BooleanSettingSpec
import me.inkdye.zuxos.ui.settings.SettingsRepository
import me.inkdye.zuxos.ui.settings.rememberBooleanSettingState
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun BooleanSettingSwitch(
    spec: BooleanSettingSpec,
    repository: SettingsRepository,
    colorScheme: AppColorScheme = LightColorScheme,
) {
    val state = rememberBooleanSettingState(repository, spec)
    GlassSwitch(
        title = stringResource(spec.titleRes),
        description = stringResource(spec.descRes),
        checked = state.checked.value,
        onCheckedChange = { state.update(it) },
        colorScheme = colorScheme,
    )
}
