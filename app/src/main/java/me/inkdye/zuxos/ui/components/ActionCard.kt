package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun ActionCard(
    title: String,
    description: String,
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassCard(
        colorScheme = colorScheme,
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick,
        ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
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
    }
}
