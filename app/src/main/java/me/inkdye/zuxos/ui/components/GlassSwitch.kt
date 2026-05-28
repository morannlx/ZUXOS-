package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun GlassSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
) {
    GlassCard(colorScheme = colorScheme, modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colorScheme.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    description,
                    fontSize = 12.sp,
                    color = colorScheme.textDim,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.accentGreen,
                    checkedTrackColor = colorScheme.accentGreen.copy(alpha = if (colorScheme.isDark) 0.30f else 0.22f),
                    uncheckedThumbColor = colorScheme.textSecondary,
                    uncheckedTrackColor = colorScheme.glassSurface.copy(alpha = if (colorScheme.isDark) 0.58f else 0.64f),
                ),
            )
        }
    }
}
