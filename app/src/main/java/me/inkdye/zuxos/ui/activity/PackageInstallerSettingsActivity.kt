package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RestartScope
import me.inkdye.zuxos.ui.components.GlassSwitch
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.RestartScopeCard
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.root.rememberRootUiState
import me.inkdye.zuxos.ui.settings.PackageInstallerFeatureSettings
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberBooleanSettingState
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class PackageInstallerSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PackageInstallerSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun PackageInstallerSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))
    val coroutineScope = rememberCoroutineScope()
    val rootState = rememberRootUiState()
    var restartMessage by remember { mutableStateOf<String?>(null) }
    var restartFailed by remember { mutableStateOf(false) }

    val bgBrush = Brush.verticalGradient(
        listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)
    )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bgBrush)
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
                    title = stringResource(R.string.package_installer_settings_title),
                    subtitle = stringResource(R.string.package_installer_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            PackageInstallerFeatureSettings.forEach { spec ->
                item {
                    val state = rememberBooleanSettingState(repository, spec)
                    GlassSwitch(
                        title = stringResource(spec.titleRes),
                        description = stringResource(spec.descRes),
                        checked = state.checked.value,
                        onCheckedChange = { state.update(it) },
                        colorScheme = cs,
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                RestartScopeCard(
                    scope = RestartScope.PACKAGE_INSTALLER,
                    colorScheme = cs,
                    onRestart = {
                        rootState.restart(coroutineScope, RestartScope.PACKAGE_INSTALLER) { result ->
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
