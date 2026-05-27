package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun GlassCard(
    colorScheme: AppColorScheme = LightColorScheme,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.shape = shape
                this.clip = true
                this.shadowElevation = 4.dp.toPx()
                this.ambientShadowColor = colorScheme.cardShadow
                this.spotShadowColor = colorScheme.cardShadow
            }
            .background(colorScheme.cardBackground, shape)
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (colorScheme.isDark) 0.04f else 0.06f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = size.height * 0.3f,
                    )
                )
            }
    ) {
        content()
    }
}
