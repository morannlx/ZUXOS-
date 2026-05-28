package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun PageHeader(
    title: String,
    subtitle: String,
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colorScheme.accentBlue.copy(alpha = if (colorScheme.isDark) 0.18f else 0.1f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onBack,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("←", fontSize = 18.sp, color = colorScheme.accentBlue)
                }
                Spacer(Modifier.width(12.dp))
            }
            Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorScheme.textPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 14.sp, color = colorScheme.textSecondary)
        Spacer(Modifier.height(16.dp))
    }
}
