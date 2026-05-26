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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highcapable.betterandroid.ui.component.activity.AppViewsActivity
import me.inkdye.zuxos.R
import me.inkdye.zuxos.config.ModuleConfig

private val AccentBlue = Color(0xFF3B82F6)
private val AccentGreen = Color(0xFF22C55E)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val BgGradientTop = Color(0xFFE8F5E9)
private val BgGradientMid = Color(0xFFE3F2FD)
private val BgGradientBottom = Color(0xFFF3E5F5)

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

    val switches = listOf(
        Triple("app_info_show_package_name", R.string.app_info_show_package_name, R.string.app_info_show_package_name_desc),
        Triple("app_info_show_sdk_version", R.string.app_info_show_sdk_version, R.string.app_info_show_sdk_version_desc),
        Triple("app_info_show_install_time", R.string.app_info_show_install_time, R.string.app_info_show_install_time_desc),
        Triple("app_info_show_update_time", R.string.app_info_show_update_time, R.string.app_info_show_update_time_desc),
        Triple("app_info_show_install_source", R.string.app_info_show_install_source, R.string.app_info_show_install_source_desc),
        Triple("app_info_enable_long_copy", R.string.app_info_enable_long_copy, R.string.app_info_enable_long_copy_desc),
    )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientTop, BgGradientMid, BgGradientBottom)))
            .safeContentPadding()
    ) {
        val isTablet = maxWidth >= 600.dp
        val columns = if (isTablet) 3 else 2

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.1f))
                                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("←", fontSize = 18.sp, color = AccentBlue)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.app_info_settings_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.app_info_settings_desc), fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                }
            }

            items(switches) { (key, titleRes, descRes) ->
                SwitchGridCard(key, titleRes, descRes)
            }
        }
    }
}

@Composable
private fun SwitchGridCard(key: String, titleRes: Int, descRes: Int) {
    val context = LocalContext.current
    var checked by remember { mutableStateOf(ModuleConfig.loadBoolean(context, key, false)) }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(titleRes),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        ModuleConfig.saveBoolean(context, key, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AccentGreen,
                        checkedThumbColor = Color.White,
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                stringResource(descRes),
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
