package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun ActionText(
    text: String,
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.accentBlue,
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick,
        ),
    )
}
