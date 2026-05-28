package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.highcapable.betterandroid.ui.component.activity.AppViewsActivity
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.BooleanSettingSwitch
import me.inkdye.zuxos.ui.components.GlassSwitch
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.settings.AppInfoDetailSettings
import me.inkdye.zuxos.ui.settings.AppInfoFeatureSetting
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberBooleanSettingState
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
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
    val settings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val featureState = rememberBooleanSettingState(repository, AppInfoFeatureSetting)
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
                PageHeader(
                    title = stringResource(R.string.app_info_settings_title),
                    subtitle = stringResource(R.string.app_info_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            item(span = { GridItemSpan(columns) }) {
                GlassSwitch(
                    title = stringResource(AppInfoFeatureSetting.titleRes),
                    description = stringResource(AppInfoFeatureSetting.descRes),
                    checked = featureState.checked.value,
                    onCheckedChange = { featureState.update(it) },
                    colorScheme = cs,
                )
            }

            if (featureState.checked.value) {
                items(AppInfoDetailSettings) { spec ->
                    BooleanSettingSwitch(
                        spec = spec,
                        repository = repository,
                        colorScheme = cs,
                    )
                }
            }
        }
    }
}
