package me.inkdye.zuxos.ui.activity

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RestartScope
import me.inkdye.zuxos.ui.components.BooleanSettingSwitch
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.LiquidGlassSlider
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.RestartScopeCard
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.root.rememberRootUiState
import me.inkdye.zuxos.ui.settings.ColorSettingSpec
import me.inkdye.zuxos.ui.settings.FloatSettingSpec
import me.inkdye.zuxos.ui.settings.LenovoSystemUIControlCenterColorSettings
import me.inkdye.zuxos.ui.settings.LenovoSystemUIControlCenterFloatSettings
import me.inkdye.zuxos.ui.settings.LenovoSystemUIControlCenterMasterSetting
import me.inkdye.zuxos.ui.settings.SettingKeys
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark
import kotlin.math.roundToInt

class LenovoSystemUIControlCenterSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LenovoSystemUIControlCenterSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun LenovoSystemUIControlCenterSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val coroutineScope = rememberCoroutineScope()
    val rootState = rememberRootUiState()
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))
    var restartMessage by remember { mutableStateOf<String?>(null) }
    var restartFailed by remember { mutableStateOf(false) }
    val inactiveColorSpec = remember {
        LenovoSystemUIControlCenterColorSettings.requiredColorSetting(
            SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR,
        )
    }
    val activeColorSpec = remember {
        LenovoSystemUIControlCenterColorSettings.requiredColorSetting(
            SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR,
        )
    }
    val panelAlphaSpec = remember {
        LenovoSystemUIControlCenterFloatSettings.requiredFloatSetting(
            SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA,
        )
    }
    val pullDownBlurSpec = remember {
        LenovoSystemUIControlCenterFloatSettings.requiredFloatSetting(
            SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS,
        )
    }
    val inactiveAlphaSpec = remember {
        LenovoSystemUIControlCenterFloatSettings.requiredFloatSetting(
            SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA,
        )
    }
    val activeAlphaSpec = remember {
        LenovoSystemUIControlCenterFloatSettings.requiredFloatSetting(
            SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA,
        )
    }

    val bgBrush = Brush.verticalGradient(
        listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)
    )
    val backdrop = rememberLayerBackdrop {
        drawRect(bgBrush)
        drawContent()
    }
    val liquidGlass = settings.liquidGlassEnabled.value

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bgBrush)
            .safeContentPadding()
    ) {
        val horizontalPadding = pageHorizontalPadding()
        val useAnchoredWideLayout = maxWidth >= 1180.dp
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
                .layerBackdrop(backdrop),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PageHeader(
                    title = stringResource(R.string.lenovo_systemui_control_center_settings_title),
                    subtitle = stringResource(R.string.lenovo_systemui_control_center_settings_desc),
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            if (useAnchoredWideLayout) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    WideControlCenterSettingsLayout(
                        repository = repository,
                        colorScheme = cs,
                        inactiveColorSpec = inactiveColorSpec,
                        activeColorSpec = activeColorSpec,
                        panelAlphaSpec = panelAlphaSpec,
                        pullDownBlurSpec = pullDownBlurSpec,
                        inactiveAlphaSpec = inactiveAlphaSpec,
                        activeAlphaSpec = activeAlphaSpec,
                        backdrop = backdrop,
                        liquidGlass = liquidGlass,
                    )
                }
            } else {
                item {
                    BooleanSettingSwitch(
                        spec = LenovoSystemUIControlCenterMasterSetting,
                        repository = repository,
                        colorScheme = cs,
                    )
                }

                items(LenovoSystemUIControlCenterColorSettings) { spec ->
                    ColorSettingCard(
                        spec = spec,
                        colorScheme = cs,
                        backdrop = backdrop,
                        liquidGlass = liquidGlass,
                    )
                }

                items(LenovoSystemUIControlCenterFloatSettings) { spec ->
                    FloatSettingCard(
                        spec = spec,
                        colorScheme = cs,
                        backdrop = backdrop,
                        liquidGlass = liquidGlass,
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                RestartScopeCard(
                    scope = RestartScope.SYSTEMUI,
                    colorScheme = cs,
                    onRestart = {
                        rootState.restart(coroutineScope, RestartScope.SYSTEMUI) { result ->
                            restartMessage = result.message
                            restartFailed = !result.success
                        }
                    },
                )
            }

            restartMessage?.let { message ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = if (restartFailed) cs.accentRed else cs.accentGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun WideControlCenterSettingsLayout(
    repository: me.inkdye.zuxos.ui.settings.SettingsRepository,
    colorScheme: AppColorScheme,
    inactiveColorSpec: ColorSettingSpec,
    activeColorSpec: ColorSettingSpec,
    panelAlphaSpec: FloatSettingSpec,
    pullDownBlurSpec: FloatSettingSpec,
    inactiveAlphaSpec: FloatSettingSpec,
    activeAlphaSpec: FloatSettingSpec,
    backdrop: Backdrop,
    liquidGlass: Boolean,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BooleanSettingSwitch(
                spec = LenovoSystemUIControlCenterMasterSetting,
                repository = repository,
                colorScheme = colorScheme,
            )
            FloatSettingCard(
                spec = panelAlphaSpec,
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
            )
            FloatSettingCard(
                spec = pullDownBlurSpec,
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
            )
        }
        ColorSettingCard(
            spec = inactiveColorSpec,
            colorScheme = colorScheme,
            backdrop = backdrop,
            liquidGlass = liquidGlass,
            modifier = Modifier.weight(1f),
        )
        ColorSettingCard(
            spec = activeColorSpec,
            colorScheme = colorScheme,
            backdrop = backdrop,
            liquidGlass = liquidGlass,
            modifier = Modifier.weight(1f),
        )
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FloatSettingCard(
                spec = inactiveAlphaSpec,
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
            )
            FloatSettingCard(
                spec = activeAlphaSpec,
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
            )
        }
    }
}

private fun List<FloatSettingSpec>.requiredFloatSetting(key: String): FloatSettingSpec =
    first { it.key == key }

private fun List<ColorSettingSpec>.requiredColorSetting(key: String): ColorSettingSpec =
    first { it.key == key }

@Composable
private fun FloatSettingCard(
    spec: FloatSettingSpec,
    colorScheme: AppColorScheme,
    backdrop: Backdrop?,
    liquidGlass: Boolean,
    modifier: Modifier = Modifier,
) {
    val repository = rememberSettingsRepository()
    var value by remember(spec.key) { mutableFloatStateOf(repository.getFloat(spec)) }
    val isAlpha = spec.valueRange.endInclusive <= 1f
    val displayValue = if (isAlpha) "${(value * 100f).roundToInt()}%" else "${value.roundToInt()} px"

    GlassCard(colorScheme = colorScheme, modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(spec.titleRes),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(spec.descRes),
                        fontSize = 12.sp,
                        color = colorScheme.textDim,
                    )
                }
                Text(
                    text = displayValue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colorScheme.accentBlue,
                )
            }
            LiquidGlassSlider(
                value = value,
                onValueChange = {
                    value = it.coerceIn(spec.valueRange)
                    repository.setFloat(spec, value)
                },
                valueRange = spec.valueRange,
                activeColor = colorScheme.accentGreen,
                inactiveColor = colorScheme.glassSurface.copy(alpha = if (colorScheme.isDark) 0.56f else 0.76f),
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
            )
        }
    }
}

@Composable
private fun ColorSettingCard(
    spec: ColorSettingSpec,
    colorScheme: AppColorScheme,
    backdrop: Backdrop?,
    liquidGlass: Boolean,
    modifier: Modifier = Modifier,
) {
    val repository = rememberSettingsRepository()
    var color by remember(spec.key) { mutableStateOf(repository.getColor(spec)) }
    var text by remember(spec.key) { mutableStateOf(color.toHexArgb()) }
    val parsedColor = parseArgbColor(text)
    val hsv = remember(color) {
        FloatArray(3).apply { AndroidColor.colorToHSV(color, this) }
    }
    val alpha = AndroidColor.alpha(color)

    fun updateColor(newColor: Int) {
        color = newColor
        text = newColor.toHexArgb()
        repository.setColor(spec, newColor)
    }

    GlassCard(colorScheme = colorScheme, modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .background(Color(color), CircleShape)
                        .border(1.dp, colorScheme.glassBorder, CircleShape)
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(spec.titleRes),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(spec.descRes),
                        fontSize = 12.sp,
                        color = colorScheme.textDim,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            ArgbColorPicker(
                color = color,
                hsv = hsv,
                alpha = alpha,
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
                onColorChange = ::updateColor,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    text = input
                    parseArgbColor(input)?.let { parsed ->
                        updateColor(parsed)
                    }
                },
                isError = parsedColor == null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = stringResource(R.string.lenovo_systemui_control_center_color_format_hint),
                        color = colorScheme.textDim,
                    )
                },
            )
        }
    }
}

@Composable
private fun ArgbColorPicker(
    color: Int,
    hsv: FloatArray,
    alpha: Int,
    colorScheme: AppColorScheme,
    backdrop: Backdrop?,
    liquidGlass: Boolean,
    onColorChange: (Int) -> Unit,
) {
    val hue = hsv[0]
    val saturation = hsv[1]
    val value = hsv[2]

    ColorPlane(
        hue = hue,
        saturation = saturation,
        brightness = value,
        colorScheme = colorScheme,
        onChange = { nextSaturation, nextValue ->
            onColorChange(AndroidColor.HSVToColor(alpha, floatArrayOf(hue, nextSaturation, nextValue)))
        },
    )
    Spacer(Modifier.height(10.dp))
    HueStrip(
        hue = hue,
        colorScheme = colorScheme,
        onHueChange = { nextHue ->
            onColorChange(AndroidColor.HSVToColor(alpha, floatArrayOf(nextHue, saturation, value)))
        },
    )
    Spacer(Modifier.height(10.dp))
    ChannelSliderRow(
        label = "A",
        value = alpha,
        activeColor = Color(color).copy(alpha = 1f),
        colorScheme = colorScheme,
        backdrop = backdrop,
        liquidGlass = liquidGlass,
        onChange = { nextAlpha ->
            onColorChange(AndroidColor.argb(nextAlpha, AndroidColor.red(color), AndroidColor.green(color), AndroidColor.blue(color)))
        },
    )
    ChannelSliderRow(
        label = "R",
        value = AndroidColor.red(color),
        activeColor = Color(0xFFE58B8B),
        colorScheme = colorScheme,
        backdrop = backdrop,
        liquidGlass = liquidGlass,
        onChange = { next ->
            onColorChange(AndroidColor.argb(alpha, next, AndroidColor.green(color), AndroidColor.blue(color)))
        },
    )
    ChannelSliderRow(
        label = "G",
        value = AndroidColor.green(color),
        activeColor = Color(0xFF74B99A),
        colorScheme = colorScheme,
        backdrop = backdrop,
        liquidGlass = liquidGlass,
        onChange = { next ->
            onColorChange(AndroidColor.argb(alpha, AndroidColor.red(color), next, AndroidColor.blue(color)))
        },
    )
    ChannelSliderRow(
        label = "B",
        value = AndroidColor.blue(color),
        activeColor = Color(0xFF82A7D6),
        colorScheme = colorScheme,
        backdrop = backdrop,
        liquidGlass = liquidGlass,
        onChange = { next ->
            onColorChange(AndroidColor.argb(alpha, AndroidColor.red(color), AndroidColor.green(color), next))
        },
    )
}

@Composable
private fun ColorPlane(
    hue: Float,
    saturation: Float,
    brightness: Float,
    colorScheme: AppColorScheme,
    onChange: (Float, Float) -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val hueColor = Color.hsv(hue, 1f, 1f)

    fun update(offset: Offset) {
        val width = size.width.coerceAtLeast(1)
        val height = size.height.coerceAtLeast(1)
        onChange((offset.x / width).coerceIn(0f, 1f), (1f - offset.y / height).coerceIn(0f, 1f))
    }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(colorScheme.glassSurface, RoundedCornerShape(12.dp))
            .onSizeChanged { size = it }
            .pointerInput(hue) {
                detectTapGestures { update(it) }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ -> update(change.position) }
            }
    ) {
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color.White, hueColor)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
        val x = saturation * size.width
        val y = (1f - brightness) * size.height
        drawLine(Color.White, Offset(x - 8.dp.toPx(), y), Offset(x + 8.dp.toPx(), y), strokeWidth = 2.dp.toPx())
        drawLine(Color.White, Offset(x, y - 8.dp.toPx()), Offset(x, y + 8.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawRoundRect(
            color = colorScheme.glassBorder,
            style = Stroke(width = 1.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
    }
}

@Composable
private fun HueStrip(hue: Float, colorScheme: AppColorScheme, onHueChange: (Float) -> Unit) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    fun update(x: Float) {
        val width = size.width.coerceAtLeast(1)
        onHueChange((x / width).coerceIn(0f, 1f) * 360f)
    }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(24.dp)
            .onSizeChanged { size = it }
            .pointerInput(Unit) { detectTapGestures { update(it.x) } }
            .pointerInput(Unit) { detectDragGestures { change, _ -> update(change.position.x) } }
    ) {
        val colors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red,
        )
        val top = 6.dp.toPx()
        val height = 10.dp.toPx()
        drawRoundRect(
            brush = Brush.horizontalGradient(colors),
            topLeft = Offset(0f, top),
            size = androidx.compose.ui.geometry.Size(size.width.toFloat(), height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f, height / 2f),
        )
        val x = (hue / 360f).coerceIn(0f, 1f) * size.width
        drawLine(colorScheme.textPrimary, Offset(x, 3.dp.toPx()), Offset(x, 21.dp.toPx()), strokeWidth = 2.dp.toPx())
    }
}

@Composable
private fun ChannelSliderRow(
    label: String,
    value: Int,
    activeColor: Color,
    colorScheme: AppColorScheme,
    backdrop: Backdrop?,
    liquidGlass: Boolean,
    onChange: (Int) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.textSecondary, modifier = Modifier.width(14.dp))
        LiquidGlassSlider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt().coerceIn(0, 255)) },
            valueRange = 0f..255f,
            activeColor = activeColor,
            inactiveColor = colorScheme.glassSurface.copy(alpha = if (colorScheme.isDark) 0.50f else 0.78f),
            colorScheme = colorScheme,
            backdrop = backdrop,
            liquidGlass = liquidGlass,
            modifier = Modifier.weight(1f),
        )
        Text(value.toString(), fontSize = 12.sp, color = colorScheme.textDim, modifier = Modifier.width(30.dp))
    }
}

private fun Int.toHexArgb(): String =
    "#%08X".format(this.toLong() and 0xFFFFFFFFL)

private fun parseArgbColor(value: String): Int? {
    val normalized = value.trim().removePrefix("#")
    if (normalized.length != 6 && normalized.length != 8) return null
    return runCatching {
        val argb = if (normalized.length == 6) "FF$normalized" else normalized
        argb.toLong(16).toInt()
    }.getOrNull()
}
