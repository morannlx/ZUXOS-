package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.LiquidGlassCardBackdropProvider
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.settings.LenovoDesktopModeRotationModeSetting
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberIntSettingState
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class WorkDesktopSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkDesktopSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun WorkDesktopSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
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
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
                    .padding(horizontal = horizontalPadding),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PageHeader(
                        title = stringResource(R.string.work_desktop_settings_title),
                        subtitle = stringResource(R.string.work_desktop_settings_desc),
                        colorScheme = cs,
                        onBack = onBack,
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DesktopModeRotationModeCard(colorScheme = cs)
                }
            }
        }
    }
}

@Composable
private fun DesktopModeRotationModeCard(colorScheme: AppColorScheme) {
    val repository = rememberSettingsRepository()
    val state = rememberIntSettingState(repository, LenovoDesktopModeRotationModeSetting)
    val options = listOf(
        0 to stringResource(R.string.lenovo_desktop_mode_rotation_auto),
        1 to stringResource(R.string.lenovo_desktop_mode_rotation_landscape),
        2 to stringResource(R.string.lenovo_desktop_mode_rotation_reverse_landscape),
        3 to stringResource(R.string.lenovo_desktop_mode_rotation_portrait),
        4 to stringResource(R.string.lenovo_desktop_mode_rotation_reverse_portrait),
    )

    GlassCard(colorScheme = colorScheme) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(LenovoDesktopModeRotationModeSetting.titleRes),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colorScheme.textPrimary,
            )
            Text(
                text = stringResource(LenovoDesktopModeRotationModeSetting.descRes),
                fontSize = 12.sp,
                color = colorScheme.textDim,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { (value, label) ->
                    FilterChip(
                        selected = state.value.value == value,
                        onClick = { state.update(value) },
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}
