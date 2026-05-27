package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme
import me.inkdye.zuxos.ui.theme.ThemeMode

@Composable
fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
) {
    GlassCard(colorScheme = colorScheme, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Theme Mode",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colorScheme.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Choose light, dark, or follow system",
                fontSize = 12.sp,
                color = colorScheme.textDim,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val itemShape = RoundedCornerShape(12.dp)

                    Box(
                        Modifier
                            .weight(1f)
                            .clip(itemShape)
                            .background(
                                if (isSelected) colorScheme.accentGreen.copy(alpha = 0.15f)
                                else colorScheme.glassSurface.copy(alpha = 0.5f),
                                itemShape,
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { onModeSelected(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = when (mode) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "System"
                            },
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colorScheme.accentGreen else colorScheme.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
