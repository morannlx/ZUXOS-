package me.inkdye.zuxos.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.logging.HookLogEntry
import me.inkdye.zuxos.logging.HookLogLevel
import me.inkdye.zuxos.logging.HookLogStore
import me.inkdye.zuxos.ui.components.ActionCard
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.theme.AppColorScheme

@Composable
fun HookLogsPage(cs: AppColorScheme) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(emptyList<HookLogEntry>()) }
    var filter by remember { mutableStateOf("") }
    val horizontalPadding = pageHorizontalPadding()

    fun reload() {
        entries = HookLogStore.read(context)
    }

    LaunchedEffect(Unit) { reload() }

    val filtered = remember(entries, filter) {
        val query = filter.trim()
        if (query.isBlank()) entries else entries.filter { entry ->
            entry.hookName.contains(query, ignoreCase = true) ||
                entry.moduleName.contains(query, ignoreCase = true) ||
                entry.targetPackage.contains(query, ignoreCase = true)
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(stringResource(R.string.page_logs_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.page_logs_subtitle), fontSize = 14.sp, color = cs.textSecondary)
            Spacer(Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.logs_filter_hint)) },
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = stringResource(R.string.logs_refresh),
                    description = stringResource(R.string.logs_refresh_desc),
                    colorScheme = cs,
                    modifier = Modifier.weight(1f),
                    onClick = { reload() },
                )
                ActionCard(
                    title = stringResource(R.string.logs_clear),
                    description = stringResource(R.string.logs_clear_desc),
                    colorScheme = cs,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        HookLogStore.clear(context)
                        reload()
                    },
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                Text(stringResource(R.string.logs_empty), fontSize = 14.sp, color = cs.textDim)
            }
        } else {
            items(filtered) { entry ->
                HookLogCard(entry, cs)
            }
        }
    }
}

@Composable
private fun HookLogCard(entry: HookLogEntry, cs: AppColorScheme) {
    val levelColor = when (entry.level) {
        HookLogLevel.INFO -> cs.accentGreen
        HookLogLevel.WARN -> cs.accentBlue
        HookLogLevel.ERROR -> cs.accentRed
    }
    GlassCard(colorScheme = cs) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.hookName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = cs.textPrimary)
                Text(entry.stage.name, fontSize = 11.sp, color = levelColor)
            }
            Text(entry.formattedTime, fontSize = 11.sp, color = cs.textDim)
            Text("${entry.moduleName} / ${entry.targetPackage}", fontSize = 12.sp, color = cs.textSecondary)
            Text(entry.processName, fontSize = 12.sp, color = cs.textSecondary)
            Text(entry.message, fontSize = 12.sp, color = cs.textPrimary, lineHeight = 17.sp)
            entry.stackTrace?.let {
                Text(it, fontSize = 10.sp, color = cs.accentRed, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
            }
        }
    }
}
