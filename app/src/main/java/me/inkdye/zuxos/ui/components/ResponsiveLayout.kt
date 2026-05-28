package me.inkdye.zuxos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun pageHorizontalPadding(): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    return when {
        screenWidth < 360 -> 12.dp
        screenWidth < 600 -> 14.dp
        screenWidth < 840 -> 20.dp
        else -> 28.dp
    }
}
