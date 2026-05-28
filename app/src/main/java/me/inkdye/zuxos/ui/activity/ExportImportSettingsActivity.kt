package me.inkdye.zuxos.ui.activity

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.ActionCard
import me.inkdye.zuxos.ui.components.AppBackground
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class ExportImportSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExportImportSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun ExportImportSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val appSettings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val cs = colorSchemeFor(resolveIsDark(appSettings.themeMode.value))
    var statusText by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun setSuccess(messageRes: Int) {
        statusText = context.getString(messageRes)
        isError = false
    }

    fun setFailure(error: Throwable) {
        statusText = context.getString(
            R.string.settings_operation_failed,
            error.localizedMessage ?: error.javaClass.simpleName,
        )
        isError = true
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(repository.exportToJson().toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open output stream")
        }.onSuccess {
            setSuccess(R.string.export_settings_success)
        }.onFailure { error ->
            setFailure(error)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("Unable to open input stream")
            repository.importFromJson(json)
            appSettings.syncFromPreferences()
        }.onSuccess {
            setSuccess(R.string.import_settings_success)
        }.onFailure { error ->
            setFailure(error)
        }
    }

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
                    title = stringResource(R.string.export_settings_title),
                    subtitle = stringResource(R.string.export_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.export_settings_export),
                    description = stringResource(R.string.export_settings_export_desc),
                    colorScheme = cs,
                    onClick = { exportLauncher.launch("zuxos-settings.json") },
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.export_settings_import),
                    description = stringResource(R.string.export_settings_import_desc),
                    colorScheme = cs,
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                )
            }

            statusText?.let { message ->
                item {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = if (isError) cs.accentRed else cs.accentGreen,
                    )
                }
            }
        }
    }
}
