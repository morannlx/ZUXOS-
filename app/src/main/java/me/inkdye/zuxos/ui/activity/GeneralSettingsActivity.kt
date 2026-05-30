package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.ActionCard
import me.inkdye.zuxos.ui.components.AppBackground
import me.inkdye.zuxos.ui.components.BooleanSettingSwitch
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.settings.HookLoggingSetting
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class GeneralSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeneralSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun GeneralSettingsPage(onBack: () -> Unit) {
    val appSettings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val cs = colorSchemeFor(
        resolveIsDark(appSettings.themeMode.value),
        titleTextContrast = appSettings.titleTextContrast.value,
        descriptionTextContrast = appSettings.descriptionTextContrast.value,
    )
    val resetDone = stringResource(R.string.settings_reset_done)
    var statusText by remember { mutableStateOf<String?>(null) }

    AppBackground(colorScheme = cs) {
        val horizontalPadding = pageHorizontalPadding()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = stringResource(R.string.general_settings_title),
                    subtitle = stringResource(R.string.general_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            item {
                BooleanSettingSwitch(
                    spec = HookLoggingSetting,
                    repository = repository,
                    colorScheme = cs,
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.settings_reset_defaults),
                    description = stringResource(R.string.settings_reset_defaults_desc),
                    colorScheme = cs,
                    onClick = {
                        repository.resetToDefaults()
                        appSettings.syncFromPreferences()
                        statusText = resetDone
                    },
                )
            }

            statusText?.let { message ->
                item {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = cs.accentGreen,
                    )
                }
            }
        }
    }
}
