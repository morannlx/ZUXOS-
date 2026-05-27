package me.inkdye.zuxos.ui.activity

import android.os.Bundle
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highcapable.betterandroid.ui.component.activity.AppViewsActivity
import me.inkdye.zuxos.R
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class AppInfoSettingsActivity : AppViewsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppInfoSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun AppInfoSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))

    val switches = listOf(
        Triple("app_info_show_package_name", R.string.app_info_show_package_name, R.string.app_info_show_package_name_desc),
        Triple("app_info_show_sdk_version", R.string.app_info_show_sdk_version, R.string.app_info_show_sdk_version_desc),
        Triple("app_info_show_install_time", R.string.app_info_show_install_time, R.string.app_info_show_install_time_desc),
        Triple("app_info_show_update_time", R.string.app_info_show_update_time, R.string.app_info_show_update_time_desc),
        Triple("app_info_show_install_source", R.string.app_info_show_install_source, R.string.app_info_show_install_source_desc),
        Triple("app_info_enable_long_copy", R.string.app_info_enable_long_copy, R.string.app_info_enable_long_copy_desc),
    )

    val bgBrush = Brush.verticalGradient(
        listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)
    )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bgBrush)
            .safeContentPadding()
    ) {
        val isTablet = maxWidth >= 600.dp
        val columns = if (isTablet) 3 else 2

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(columns) }) {
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
                        Text(stringResource(R.string.app_info_settings_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.app_info_settings_desc), fontSize = 14.sp, color = cs.textSecondary)
                    Spacer(Modifier.height(16.dp))
                }
            }

            items(switches) { (key, titleRes, descRes) ->
                SwitchGridCard(key, titleRes, descRes, cs)
            }
        }
    }
}

@Composable
private fun SwitchGridCard(key: String, titleRes: Int, descRes: Int, cs: AppColorScheme) {
    val context = LocalContext.current
    var checked by remember { mutableStateOf(ModuleConfig.loadBoolean(context, key, false)) }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cs.cardBackground)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                checked = !checked
                ModuleConfig.saveBoolean(context, key, checked)
            }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(titleRes),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = cs.accentBlue,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        ModuleConfig.saveBoolean(context, key, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = cs.accentGreen,
                        checkedTrackColor = cs.accentGreen.copy(alpha = 0.3f),
                        uncheckedThumbColor = cs.textDim,
                        uncheckedTrackColor = cs.glassSurface,
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                stringResource(descRes),
                fontSize = 12.sp,
                color = cs.textSecondary,
                lineHeight = 18.sp,
            )
        }
    }
}
