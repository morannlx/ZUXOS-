package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun AppBackground(
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val bgGradient = Brush.verticalGradient(
        listOf(colorScheme.bgGradientTop, colorScheme.bgGradientMid, colorScheme.bgGradientBottom)
    )

    Box(
        modifier
            .fillMaxSize()
            .background(bgGradient)
            .safeContentPadding(),
        content = content,
    )
}
