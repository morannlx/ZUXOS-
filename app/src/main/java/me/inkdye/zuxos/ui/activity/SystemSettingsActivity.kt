package me.inkdye.zuxos.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.LiquidGlassCardBackdropProvider
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class SystemSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SystemSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun SystemSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    val context = LocalContext.current

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
                        title = stringResource(R.string.system_settings_title),
                        subtitle = stringResource(R.string.system_settings_desc),
                        colorScheme = cs,
                        onBack = onBack,
                    )
                }
                item {
                    SettingGridCard(
                        title = stringResource(R.string.display_size_function),
                        desc = stringResource(R.string.display_size_function_desc),
                        cs = cs,
                        onClick = {
                            context.startActivity(Intent(context, DisplaySizeSettingsActivity::class.java))
                        },
                    )
                }
                item {
                    SettingGridCard(
                        title = stringResource(R.string.app_info_function),
                        desc = stringResource(R.string.app_info_function_desc),
                        cs = cs,
                        onClick = {
                            context.startActivity(Intent(context, AppInfoSettingsActivity::class.java))
                        },
                    )
                }
                item {
                    SettingGridCard(
                        title = stringResource(R.string.theme_function),
                        desc = stringResource(R.string.theme_function_desc),
                        cs = cs,
                        onClick = {
                            context.startActivity(Intent(context, ThemeSettingsActivity::class.java))
                        },
                    )
                }
                item {
                    SettingGridCard(
                        title = stringResource(R.string.lenovo_about_device_function),
                        desc = stringResource(R.string.lenovo_about_device_function_desc),
                        cs = cs,
                        onClick = {
                            context.startActivity(Intent(context, LenovoAboutDeviceSettingsActivity::class.java))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingGridCard(title: String, desc: String, cs: AppColorScheme, onClick: () -> Unit) {
    GlassCard(
        colorScheme = cs,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) { onClick() },
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = cs.accentBlue)
            Text(desc, fontSize = 12.sp, color = cs.textSecondary, lineHeight = 18.sp)
        }
    }
}
