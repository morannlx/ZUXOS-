package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import me.inkdye.zuxos.ui.settings.AppPreferences
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun GlassCard(
    colorScheme: AppColorScheme = LightColorScheme,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalLiquidGlassBackdrop.current
    val liquidGlass = LocalLiquidGlassEnabled.current
    val cardOpacity = LocalGlassCardOpacity.current.coerceIn(
        AppPreferences.MIN_GLASS_CARD_OPACITY,
        AppPreferences.MAX_GLASS_CARD_OPACITY,
    )
    val baseModifier = if (liquidGlass && backdrop != null) {
        modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(9.dp.toPx())
                    lens(18.dp.toPx(), 22.dp.toPx(), depthEffect = true)
                },
                highlight = {
                    Highlight.Default.copy(alpha = if (colorScheme.isDark) 0.34f else 0.46f)
                },
                shadow = {
                    Shadow(
                        radius = 16.dp,
                        color = Color.Black.copy(alpha = if (colorScheme.isDark) 0.20f else 0.08f),
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 8.dp,
                        alpha = 0.64f,
                        color = colorScheme.glassHighlight.copy(alpha = if (colorScheme.isDark) 0.08f else 0.18f),
                    )
                },
                onDrawSurface = {
                    drawRect(colorScheme.cardBackground.copy(alpha = cardOpacity))
                    drawRect(
                        Brush.verticalGradient(
                            listOf(
                                colorScheme.glassHighlight.copy(alpha = if (colorScheme.isDark) 0.05f else 0.10f),
                                colorScheme.glassHighlight.copy(alpha = 0f),
                            ),
                            startY = 0f,
                            endY = size.height * 0.36f,
                        )
                    )
                },
            )
    } else {
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.shape = shape
                this.clip = true
                this.shadowElevation = 3.dp.toPx()
                this.ambientShadowColor = colorScheme.cardShadow
                this.spotShadowColor = colorScheme.cardShadow
            }
            .background(colorScheme.cardBackground.copy(alpha = cardOpacity), shape)
            .border(1.dp, colorScheme.glassBorder.copy(alpha = 0.40f), shape)
    }

    Box(
        baseModifier
    ) {
        content()
    }
}
