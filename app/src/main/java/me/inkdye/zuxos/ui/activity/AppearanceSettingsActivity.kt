package me.inkdye.zuxos.ui.activity

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.GlassSwitch
import me.inkdye.zuxos.ui.components.LiquidGlassCardBackdropProvider
import me.inkdye.zuxos.ui.components.LiquidGlassSlider
import me.inkdye.zuxos.ui.components.LocalLiquidGlassBackdrop
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.ThemeModeSelector
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.settings.AppPreferences
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark
import java.io.FileOutputStream
import kotlin.math.roundToInt

class AppearanceSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppearanceSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun AppearanceSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val context = LocalContext.current
    val liquidGlassEnabled = settings.liquidGlassEnabled.value
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    val customBackgroundEnabled = settings.customBackgroundEnabled.value
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusFailed by remember { mutableStateOf(false) }

    fun saveSelectedBackground(uri: Uri) {
        runCatching {
            val outputFile = AppPreferences.customBackgroundFile(context)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outputFile).use { output -> input.copyTo(output) }
            } ?: error("openInputStream returned null")
            settings.notifyCustomBackgroundChanged()
            statusFailed = false
            statusMessage = context.getString(R.string.appearance_background_saved)
        }.onFailure { error ->
            statusFailed = true
            statusMessage = context.getString(
                R.string.appearance_background_save_failed,
                error.message.orEmpty(),
            )
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) saveSelectedBackground(uri)
    }

    val bgBrush = Brush.verticalGradient(
        listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)
    )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        LiquidGlassCardBackdropProvider(
            backdropBrush = bgBrush,
            enabled = liquidGlassEnabled,
            colorScheme = cs,
        ) {
            val horizontalPadding = pageHorizontalPadding()
            LazyColumn(
                modifier = Modifier.fillMaxSize().safeContentPadding().padding(horizontal = horizontalPadding),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    PageHeader(
                        title = stringResource(R.string.appearance_title),
                        subtitle = stringResource(R.string.appearance_desc),
                        colorScheme = cs,
                        onBack = onBack,
                    )
                }

                item {
                    ThemeModeSelector(
                        selectedMode = settings.themeMode.value,
                        onModeSelected = { settings.updateThemeMode(it) },
                        colorScheme = cs,
                    )
                }

                item {
                    GlassSwitch(
                        title = stringResource(R.string.appearance_floating_nav),
                        description = stringResource(R.string.appearance_floating_nav_desc),
                        checked = settings.floatingNavEnabled.value,
                        onCheckedChange = { settings.updateFloatingNav(it) },
                        colorScheme = cs,
                    )
                }

                item {
                    GlassSwitch(
                        title = stringResource(R.string.appearance_liquid_glass),
                        description = stringResource(R.string.appearance_liquid_glass_desc),
                        checked = liquidGlassEnabled,
                        onCheckedChange = { settings.updateLiquidGlass(it) },
                        colorScheme = cs,
                    )
                }

                if (liquidGlassEnabled) {
                    item {
                        GlassCardOpacityCard(
                            colorScheme = cs,
                            value = settings.glassCardOpacity.value,
                            liquidGlass = liquidGlassEnabled,
                            onValueChange = { settings.updateGlassCardOpacity(it) },
                        )
                    }
                }

                item {
                    TextContrastCard(
                        title = stringResource(R.string.appearance_title_text_contrast),
                        description = stringResource(R.string.appearance_title_text_contrast_desc),
                        colorScheme = cs,
                        value = settings.titleTextContrast.value,
                        liquidGlass = liquidGlassEnabled,
                        onValueChange = { settings.updateTitleTextContrast(it) },
                    )
                }

                item {
                    TextContrastCard(
                        title = stringResource(R.string.appearance_description_text_contrast),
                        description = stringResource(R.string.appearance_description_text_contrast_desc),
                        colorScheme = cs,
                        value = settings.descriptionTextContrast.value,
                        liquidGlass = liquidGlassEnabled,
                        onValueChange = { settings.updateDescriptionTextContrast(it) },
                    )
                }

                item {
                    GlassSwitch(
                        title = stringResource(R.string.appearance_custom_background),
                        description = stringResource(R.string.appearance_custom_background_desc),
                        checked = customBackgroundEnabled,
                        onCheckedChange = { enabled ->
                            settings.updateCustomBackgroundEnabled(enabled)
                            statusMessage = null
                        },
                        colorScheme = cs,
                    )
                }

                if (customBackgroundEnabled) {
                    item {
                        CustomBackgroundCard(
                            colorScheme = cs,
                            backgroundVersion = settings.customBackgroundVersion.value,
                            statusMessage = statusMessage,
                            statusFailed = statusFailed,
                            onSelectImage = { imagePicker.launch("image/*") },
                            onClearImage = {
                                settings.clearCustomBackground()
                                statusFailed = false
                                statusMessage = null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextContrastCard(
    title: String,
    description: String,
    colorScheme: AppColorScheme,
    value: Float,
    liquidGlass: Boolean,
    onValueChange: (Float) -> Unit,
) {
    val backdrop = LocalLiquidGlassBackdrop.current
    val displayValue = "${(value * 100f).roundToInt()}%"

    GlassCard(colorScheme = colorScheme) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = colorScheme.textDim,
                        lineHeight = 17.sp,
                    )
                }
                Text(
                    text = displayValue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colorScheme.accentBlue,
                )
            }
            Spacer(Modifier.height(12.dp))
            LiquidGlassSlider(
                value = value,
                onValueChange = { onValueChange(it.coerceIn(AppPreferences.MIN_TEXT_CONTRAST, AppPreferences.MAX_TEXT_CONTRAST)) },
                valueRange = AppPreferences.MIN_TEXT_CONTRAST..AppPreferences.MAX_TEXT_CONTRAST,
                activeColor = colorScheme.accentGreen,
                inactiveColor = colorScheme.glassSurface.copy(alpha = if (colorScheme.isDark) 0.50f else 0.62f),
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
            )
        }
    }
}

@Composable
private fun GlassCardOpacityCard(
    colorScheme: AppColorScheme,
    value: Float,
    liquidGlass: Boolean,
    onValueChange: (Float) -> Unit,
) {
    val backdrop = LocalLiquidGlassBackdrop.current
    val displayValue = "${(value * 100f).roundToInt()}%"

    GlassCard(colorScheme = colorScheme) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.appearance_glass_card_opacity),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.appearance_glass_card_opacity_desc),
                        fontSize = 12.sp,
                        color = colorScheme.textDim,
                        lineHeight = 17.sp,
                    )
                }
                Text(
                    text = displayValue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colorScheme.accentBlue,
                )
            }
            Spacer(Modifier.height(12.dp))
            LiquidGlassSlider(
                value = value,
                onValueChange = { onValueChange(it.coerceIn(AppPreferences.MIN_GLASS_CARD_OPACITY, AppPreferences.MAX_GLASS_CARD_OPACITY)) },
                valueRange = AppPreferences.MIN_GLASS_CARD_OPACITY..AppPreferences.MAX_GLASS_CARD_OPACITY,
                activeColor = colorScheme.accentGreen,
                inactiveColor = colorScheme.glassSurface.copy(alpha = if (colorScheme.isDark) 0.50f else 0.62f),
                colorScheme = colorScheme,
                backdrop = backdrop,
                liquidGlass = liquidGlass,
            )
        }
    }
}

@Composable
private fun CustomBackgroundCard(
    colorScheme: AppColorScheme,
    backgroundVersion: Int,
    statusMessage: String?,
    statusFailed: Boolean,
    onSelectImage: () -> Unit,
    onClearImage: () -> Unit,
) {
    val context = LocalContext.current
    val hasBackground = remember(backgroundVersion) {
        AppPreferences.hasCustomBackground(context)
    }
    val preview = remember(backgroundVersion) {
        AppPreferences.customBackgroundFile(context)
            .takeIf { it.isFile }
            ?.let { BitmapFactory.decodeFile(it.absolutePath) }
    }

    GlassCard(colorScheme = colorScheme) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 140.dp, height = 84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.glassSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    if (preview != null) {
                        Image(
                            bitmap = preview.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.appearance_background_empty),
                            fontSize = 12.sp,
                            color = colorScheme.textDim,
                        )
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.appearance_custom_background),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.appearance_custom_background_desc),
                        fontSize = 12.sp,
                        color = colorScheme.textDim,
                        lineHeight = 17.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            if (hasBackground) {
                                R.string.appearance_background_selected
                            } else {
                                R.string.appearance_background_empty
                            }
                        ),
                        fontSize = 12.sp,
                        color = if (hasBackground) colorScheme.accentGreen else colorScheme.textDim,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onSelectImage) {
                    Text(stringResource(R.string.appearance_select_background))
                }
                TextButton(onClick = onClearImage, enabled = hasBackground) {
                    Text(stringResource(R.string.appearance_clear_background))
                }
            }

            statusMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = if (statusFailed) colorScheme.accentRed else colorScheme.accentGreen,
                )
            }
        }
    }
}
