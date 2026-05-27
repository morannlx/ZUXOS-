package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.GlassSwitch
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(cs.accentBlue.copy(alpha = if (cs.isDark) 0.18f else 0.1f))
                                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onBack() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("←", fontSize = 18.sp, color = cs.accentBlue)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.appearance_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.appearance_desc), fontSize = 14.sp, color = cs.textSecondary)
                    Spacer(Modifier.height(16.dp))
                }
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
