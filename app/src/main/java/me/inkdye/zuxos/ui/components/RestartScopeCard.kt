package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RestartScope
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun RestartScopeCard(
    scope: RestartScope,
    colorScheme: AppColorScheme = LightColorScheme,
    onRestart: () -> Unit,
) {
    GlassCard(colorScheme = colorScheme) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.restart_scope_title, scope.label),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colorScheme.textPrimary,
            )
            Text(
                text = stringResource(R.string.restart_scope_desc),
                fontSize = 12.sp,
                color = colorScheme.textDim,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
            )
            ActionText(
                text = stringResource(R.string.restart_scope_action),
                colorScheme = colorScheme,
                onClick = onRestart,
            )
        }
    }
}
