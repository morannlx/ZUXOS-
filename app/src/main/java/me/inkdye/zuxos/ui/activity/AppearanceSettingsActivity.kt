package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.GlassSwitch
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.ThemeModeSelector
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class AppearanceSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppearanceSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun AppearanceSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))

    val bgBrush = Brush.verticalGradient(
        listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)
    )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bgBrush)
            .safeContentPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = stringResource(R.string.appearance_title),
                    subtitle = stringResource(R.string.appearance_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            item {
                ThemeModeSelector(
                    selectedMode = settings.themeMode.value,
                    onModeSelected = { settings.updateThemeMode(it) },
                    colorScheme = cs,
                )
            }

            item {
                GlassSwitch(
                    title = stringResource(R.string.appearance_floating_nav),
                    description = stringResource(R.string.appearance_floating_nav_desc),
                    checked = settings.floatingNavEnabled.value,
                    onCheckedChange = { settings.updateFloatingNav(it) },
                    colorScheme = cs,
                )
            }

            item {
                GlassSwitch(
                    title = stringResource(R.string.appearance_liquid_glass),
                    description = stringResource(R.string.appearance_liquid_glass_desc),
                    checked = settings.liquidGlassEnabled.value,
                    onCheckedChange = { settings.updateLiquidGlass(it) },
                    colorScheme = cs,
                )
            }
        }
    }
}
