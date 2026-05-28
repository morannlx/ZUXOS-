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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RestartScope
import me.inkdye.zuxos.ui.components.GlassSwitch
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.RestartScopeCard
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.root.rememberRootUiState
import me.inkdye.zuxos.ui.settings.DisableFlagSecureSetting
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberBooleanSettingState
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class SystemFrameworkSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SystemFrameworkSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun SystemFrameworkSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val rootState = rememberRootUiState()
    val disableFlagSecureState = rememberBooleanSettingState(repository, DisableFlagSecureSetting)
    var restartMessage by remember { mutableStateOf<String?>(null) }
    var restartFailed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)))
            .safeContentPadding()
    ) {
        val horizontalPadding = pageHorizontalPadding()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 260.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PageHeader(
                    title = stringResource(R.string.system_framework_settings),
                    subtitle = stringResource(R.string.system_framework_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }
            item {
                CorePatchCard(cs = cs) {
                    context.startActivity(Intent(context, CorePatchSettingsActivity::class.java))
                }
            }
            item {
                GlassSwitch(
                    title = stringResource(DisableFlagSecureSetting.titleRes),
                    description = stringResource(DisableFlagSecureSetting.descRes),
                    checked = disableFlagSecureState.checked.value,
                    onCheckedChange = { disableFlagSecureState.update(it) },
                    colorScheme = cs,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                RestartScopeCard(
                    scope = RestartScope.ANDROID_SYSTEM,
                    colorScheme = cs,
                    onRestart = {
                        rootState.restart(coroutineScope, RestartScope.ANDROID_SYSTEM) { result ->
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

@Composable
private fun CorePatchCard(cs: AppColorScheme, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cs.cardBackground)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.core_patch_function),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = cs.accentBlue,
            )
            Text(
                text = stringResource(R.string.core_patch_function_desc),
                fontSize = 12.sp,
                color = cs.textSecondary,
                lineHeight = 18.sp,
            )
        }
    }
}
