package me.inkdye.zuxos.ui.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.AppBackground
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.InfoRow
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.settings.ModuleBooleanSettings
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class AboutSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AboutSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun AboutSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))

    AppBackground(colorScheme = cs) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = stringResource(R.string.about_settings_title),
                    subtitle = stringResource(R.string.about_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            item {
                GlassCard(colorScheme = cs) {
                    Column(Modifier.padding(18.dp)) {
                        InfoRow(stringResource(R.string.about_module), stringResource(R.string.app_name), cs)
                        Spacer(Modifier.height(10.dp))
                        InfoRow(stringResource(R.string.info_version), BuildConfig.VERSION_NAME, cs)
                        Spacer(Modifier.height(10.dp))
                        InfoRow(stringResource(R.string.about_version_code), BuildConfig.VERSION_CODE.toString(), cs)
                        Spacer(Modifier.height(10.dp))
                        InfoRow(stringResource(R.string.info_package), BuildConfig.APPLICATION_ID, cs)
                        Spacer(Modifier.height(10.dp))
                        InfoRow(stringResource(R.string.info_android), Build.VERSION.RELEASE, cs)
                        Spacer(Modifier.height(10.dp))
                        InfoRow(stringResource(R.string.info_sdk), Build.VERSION.SDK_INT.toString(), cs)
                        Spacer(Modifier.height(10.dp))
                        InfoRow(stringResource(R.string.about_config_items), ModuleBooleanSettings.size.toString(), cs)
                    }
                }
            }
        }
    }
}
