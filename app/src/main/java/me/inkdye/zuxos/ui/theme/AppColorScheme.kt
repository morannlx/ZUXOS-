package me.inkdye.zuxos.ui.theme

import androidx.compose.ui.graphics.Color

data class AppColorScheme(
    val isDark: Boolean,
    val bgGradientTop: Color,
    val bgGradientMid: Color,
    val bgGradientBottom: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDim: Color,
    val accentGreen: Color,
    val accentBlue: Color,
    val accentRed: Color,
    val glassSurface: Color,
    val glassHighlight: Color,
    val glassBorder: Color,
    val cardBackground: Color,
    val cardShadow: Color,
    val navIndicator: Color,
)

val LightColorScheme = AppColorScheme(
    isDark = false,
    bgGradientTop = Color(0xFFF7FAF8),
    bgGradientMid = Color(0xFFEFF6F4),
    bgGradientBottom = Color(0xFFF6F2F8),
    textPrimary = Color(0xFF24313A),
    textSecondary = Color(0xFF65747E),
    textDim = Color(0xFF8B99A3),
    accentGreen = Color(0xFF5FAE8B),
    accentBlue = Color(0xFF6A92C8),
    accentRed = Color(0xFFD36B6B),
    glassSurface = Color(0xF2FFFFFF),
    glassHighlight = Color.White.copy(alpha = 0.72f),
    glassBorder = Color.White.copy(alpha = 0.58f),
    cardBackground = Color.White.copy(alpha = 0.66f),
    cardShadow = Color(0xFF51616B).copy(alpha = 0.08f),
    navIndicator = Color(0xFF5FAE8B).copy(alpha = 0.12f),
)

val DarkColorScheme = AppColorScheme(
    isDark = true,
    bgGradientTop = Color(0xFF101417),
    bgGradientMid = Color(0xFF151B20),
    bgGradientBottom = Color(0xFF1B1E27),
    textPrimary = Color(0xFFE8EEF2),
    textSecondary = Color(0xFFA6B0B8),
    textDim = Color(0xFF77838C),
    accentGreen = Color(0xFF74B99A),
    accentBlue = Color(0xFF82A7D6),
    accentRed = Color(0xFFE08A8A),
    glassSurface = Color(0xFF252B31).copy(alpha = 0.72f),
    glassHighlight = Color(0xFFEEF6FA).copy(alpha = 0.16f),
    glassBorder = Color.White.copy(alpha = 0.10f),
    cardBackground = Color(0xFF252B31).copy(alpha = 0.72f),
    cardShadow = Color.Black.copy(alpha = 0.22f),
    navIndicator = Color(0xFF74B99A).copy(alpha = 0.16f),
)

fun colorSchemeFor(isDark: Boolean): AppColorScheme =
    if (isDark) DarkColorScheme else LightColorScheme
