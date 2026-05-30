package me.inkdye.zuxos.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import me.inkdye.zuxos.ui.settings.AppPreferences
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

val LocalLiquidGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }
val LocalLiquidGlassEnabled = staticCompositionLocalOf { false }
val LocalGlassCardOpacity = staticCompositionLocalOf { AppPreferences.DEFAULT_GLASS_CARD_OPACITY }

@Composable
fun LiquidGlassCardBackdropProvider(
    backdropBrush: Brush,
    enabled: Boolean,
    colorScheme: AppColorScheme? = null,
    customBackground: Bitmap? = null,
    content: @Composable () -> Unit,
) {
    val settings = rememberAppSettings()
    val settingsBackground = rememberCustomBackgroundBitmap(settings)
    val effectiveColorScheme = colorScheme ?: LightColorScheme
    val effectiveBackground = customBackground ?: settingsBackground
    val cardBackdrop = rememberLayerBackdrop()
    Box(
        Modifier
            .fillMaxSize()
            .layerBackdrop(cardBackdrop)
    ) {
        if (effectiveBackground != null) {
            CustomizableAppBackgroundSurface(
                bitmap = effectiveBackground,
                bgGradient = backdropBrush,
                colorScheme = effectiveColorScheme,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(Modifier.fillMaxSize().background(backdropBrush))
        }
    }
    CompositionLocalProvider(
        LocalLiquidGlassBackdrop provides cardBackdrop,
        LocalLiquidGlassEnabled provides enabled,
        LocalGlassCardOpacity provides settings.glassCardOpacity.value,
    ) {
        content()
    }
}

fun Modifier.liquidGlassSurface(
    backdrop: Backdrop,
    shape: Shape,
    enabled: Boolean,
    colorScheme: AppColorScheme,
    tint: Color,
    pressedProgress: Float = 0f,
    blurRadius: Dp = 10.dp,
    lensRadius: Dp = 24.dp,
    lensRefraction: Dp = 44.dp,
    exportedBackdrop: LayerBackdrop? = null,
): Modifier {
    if (!enabled) {
        return shadow(
            elevation = if (colorScheme.isDark) 10.dp else 14.dp,
            shape = shape,
            clip = false,
            ambientColor = colorScheme.cardShadow,
            spotColor = colorScheme.cardShadow,
        )
            .clip(shape)
            .background(colorScheme.glassSurface, shape)
            .border(1.dp, colorScheme.glassBorder, shape)
    }

    val scale = 1f + pressedProgress.fastCoerceIn(0f, 1f) * 0.025f
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(blurRadius.toPx())
            lens(
                lensRadius.toPx(),
                lensRefraction.toPx(),
                depthEffect = true,
                chromaticAberration = true,
            )
        },
        highlight = {
            Highlight(
                width = 1.2.dp,
                blurRadius = 18.dp,
                alpha = if (colorScheme.isDark) 0.30f else 0.46f,
                style = HighlightStyle.Default(
                    color = colorScheme.glassHighlight.copy(alpha = if (colorScheme.isDark) 0.22f else 0.40f),
                    angle = 38f,
                    falloff = 1.65f,
                ),
            )
        },
        shadow = {
            Shadow(
                radius = 24.dp,
                offset = DpOffset(0.dp, 7.dp),
                color = Color.Black.copy(alpha = if (colorScheme.isDark) 0.22f else 0.08f),
            )
        },
        innerShadow = {
            InnerShadow(
                radius = 10.dp,
                offset = DpOffset(0.dp, 1.dp),
                color = colorScheme.glassHighlight.copy(alpha = if (colorScheme.isDark) 0.08f else 0.18f),
            )
        },
        layerBlock = {
            scaleX = scale
            scaleY = scale
        },
        exportedBackdrop = exportedBackdrop,
        onDrawSurface = {
            drawRect(tint)
            drawRect(colorScheme.glassHighlight.copy(alpha = if (colorScheme.isDark) 0.03f else 0.06f))
        },
    )
}

fun Modifier.liquidGlassProgressiveBlur(
    backdrop: Backdrop,
    colorScheme: AppColorScheme,
    enabled: Boolean,
    tintIntensity: Float = 0.76f,
): Modifier {
    if (!enabled) return this

    val tint = if (colorScheme.isDark) {
        Color(0xFF171C22)
    } else {
        Color.White
    }
    return drawPlainBackdrop(
        backdrop = backdrop,
        shape = { androidx.compose.ui.graphics.RectangleShape },
        effects = {
            blur(8.dp.toPx())
        },
        onDrawSurface = {
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to tint.copy(alpha = tintIntensity),
                ),
            )
        },
    )
}

@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    inactiveColor: Color,
    colorScheme: AppColorScheme,
    backdrop: Backdrop?,
    liquidGlass: Boolean,
    modifier: Modifier = Modifier,
) {
    if (backdrop == null || !liquidGlass) {
        FlatGlassSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
            modifier = modifier,
        )
        return
    }

    val trackBackdrop = rememberLayerBackdrop()
    BoxWithConstraints(
        modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        var trackSize by remember { mutableStateOf(IntSize.Zero) }
        var pressed by remember { mutableStateOf(false) }
        val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
            .coerceIn(0f, 1f)

        fun updateFromX(x: Float) {
            val width = trackSize.width.coerceAtLeast(1)
            val nextFraction = (x / width).coerceIn(0f, 1f)
            val nextValue = valueRange.start + (valueRange.endInclusive - valueRange.start) * nextFraction
            onValueChange(nextValue)
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .layerBackdrop(trackBackdrop)
                .onSizeChanged { trackSize = it }
                .pointerInput(valueRange) {
                    detectTapGestures { offset -> updateFromX(offset.x) }
                }
                .pointerInput(valueRange) {
                    detectDragGestures(
                        onDragStart = { pressed = true },
                        onDragEnd = { pressed = false },
                        onDragCancel = { pressed = false },
                    ) { change, _ ->
                        updateFromX(change.position.x)
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Canvas(Modifier.fillMaxWidth().height(24.dp)) {
                val trackHeight = 9.dp.toPx()
                val top = (size.height - trackHeight) / 2f
                val radius = trackHeight / 2f
                drawRoundRect(
                    color = inactiveColor,
                    topLeft = Offset(0f, top),
                    size = Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
                drawRoundRect(
                    color = activeColor.copy(alpha = 0.78f),
                    topLeft = Offset(0f, top),
                    size = Size(size.width * fraction, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
            }
        }

        val thumbShape = RoundedCornerShape(12.dp)
        val thumbBackdrop = rememberCombinedBackdrop(
            backdrop,
            rememberBackdrop(trackBackdrop) { drawBackdrop ->
                val press = if (pressed) 1f else 0f
                val scaleX = lerp(0.55f, 1f, press)
                val scaleY = lerp(0.35f, 1f, press)
                scale(scaleX, scaleY) {
                    drawBackdrop()
                }
            },
        )

        Box(
            Modifier
                .size(40.dp, 24.dp)
                .align(Alignment.CenterStart)
                .then(
                    Modifier
                        .graphicsLayer {
                            val width = trackSize.width.coerceAtLeast(1).toFloat()
                            translationX = (width * fraction - size.width / 2f)
                                .fastCoerceIn(-size.width / 4f, width - size.width * 3f / 4f)
                        }
                )
                .drawBackdrop(
                    backdrop = thumbBackdrop,
                    shape = { thumbShape },
                    effects = {
                        val press = if (pressed) 1f else 0f
                        blur(8.dp.toPx() * (1f - press))
                        lens(
                            10.dp.toPx() * (0.45f + press * 0.55f),
                            14.dp.toPx() * (0.35f + press * 0.65f),
                            chromaticAberration = true,
                        )
                    },
                    highlight = {
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = if (pressed) 1f else 0.72f,
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(alpha = 0.05f),
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = if (pressed) 4.dp else 1.dp,
                            alpha = if (pressed) 1f else 0.35f,
                        )
                    },
                    layerBlock = {
                        val scale = if (pressed) 1.18f else 1f
                        scaleX = scale
                        scaleY = scale
                    },
                    onDrawSurface = {
                        drawRect(colorScheme.glassHighlight.copy(alpha = if (colorScheme.isDark) 0.10f else 0.34f))
                    },
                )
        )
    }
}

@Composable
fun FlatGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    var componentSize by remember { mutableStateOf(IntSize.Zero) }
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    fun updateFromX(x: Float) {
        val width = componentSize.width.coerceAtLeast(1)
        val nextFraction = (x / width).coerceIn(0f, 1f)
        val nextValue = valueRange.start + (valueRange.endInclusive - valueRange.start) * nextFraction
        onValueChange(nextValue)
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(24.dp)
            .onSizeChanged { componentSize = it }
            .pointerInput(valueRange) {
                detectTapGestures { updateFromX(it.x) }
            }
            .pointerInput(valueRange) {
                detectDragGestures { change, _ ->
                    updateFromX(change.position.x)
                }
            },
    ) {
        val trackHeight = 9.dp.toPx()
        val top = (size.height - trackHeight) / 2f
        val radius = trackHeight / 2f
        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(0f, top),
            size = Size(size.width, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = activeColor.copy(alpha = 0.78f),
            topLeft = Offset(0f, top),
            size = Size(size.width * fraction, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.06f),
            topLeft = Offset(0f, top),
            size = Size(size.width, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
