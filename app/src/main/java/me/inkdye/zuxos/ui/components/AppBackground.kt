package me.inkdye.zuxos.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import me.inkdye.zuxos.ui.settings.AppPreferences
import me.inkdye.zuxos.ui.settings.AppSettingsState
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme
import java.io.File

@Composable
fun AppBackground(
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val settings = rememberAppSettings()
    val bgGradient = Brush.verticalGradient(
        listOf(colorScheme.bgGradientTop, colorScheme.bgGradientMid, colorScheme.bgGradientBottom)
    )
    val customBackground = rememberCustomBackgroundBitmap(settings)
    Box(
        modifier
            .fillMaxSize(),
    ) {
        CustomizableAppBackgroundSurface(
            bitmap = customBackground,
            bgGradient = bgGradient,
            colorScheme = colorScheme,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().safeContentPadding()) {
            LiquidGlassCardBackdropProvider(
                backdropBrush = bgGradient,
                enabled = settings.liquidGlassEnabled.value,
                colorScheme = colorScheme,
                customBackground = customBackground,
            ) {
                content()
            }
        }
    }
}

@Composable
fun rememberCustomBackgroundBitmap(settings: AppSettingsState): Bitmap? {
    val context = LocalContext.current
    val enabled = settings.customBackgroundEnabled.value
    val version = settings.customBackgroundVersion.value
    return remember(enabled, version) {
        if (!enabled) {
            null
        } else {
            AppPreferences.customBackgroundFile(context)
                .takeIf { it.isFile }
                ?.let { decodeSampledBitmap(it) }
        }
    }
}

@Composable
fun CustomizableAppBackgroundSurface(
    bitmap: Bitmap?,
    bgGradient: Brush,
    colorScheme: AppColorScheme,
    modifier: Modifier = Modifier,
) {
    Box(modifier.background(bgGradient)) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (colorScheme.isDark) {
                                    Color.Black.copy(alpha = 0.22f)
                                } else {
                                    Color.White.copy(alpha = 0.08f)
                                },
                                if (colorScheme.isDark) {
                                    colorScheme.bgGradientMid.copy(alpha = 0.28f)
                                } else {
                                    colorScheme.bgGradientMid.copy(alpha = 0.16f)
                                },
                                if (colorScheme.isDark) {
                                    Color.Black.copy(alpha = 0.48f)
                                } else {
                                    Color.White.copy(alpha = 0.28f)
                                },
                            )
                        )
                    )
            )
        }
    }
}

private fun decodeSampledBitmap(file: File, maxSize: Int = 3200): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    var width = bounds.outWidth
    var height = bounds.outHeight
    while (width / 2 >= maxSize || height / 2 >= maxSize) {
        sampleSize *= 2
        width /= 2
        height /= 2
    }

    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        },
    )
}
