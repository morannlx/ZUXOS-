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
    bgGradientTop = Color(0xFFE8F5E9),
    bgGradientMid = Color(0xFFE3F2FD),
    bgGradientBottom = Color(0xFFF3E5F5),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF6B7280),
    textDim = Color(0xFF9CA3AF),
    accentGreen = Color(0xFF22C55E),
    accentBlue = Color(0xFF3B82F6),
    accentRed = Color(0xFFEF4444),
    glassSurface = Color.White,
    glassHighlight = Color.White,
    glassBorder = Color.White.copy(alpha = 0.5f),
    cardBackground = Color.White,
    cardShadow = Color.Black.copy(alpha = 0.06f),
    navIndicator = Color(0xFF22C55E).copy(alpha = 0.12f),
)

val DarkColorScheme = AppColorScheme(
    isDark = true,
    bgGradientTop = Color(0xFF0D1117),
    bgGradientMid = Color(0xFF161B22),
    bgGradientBottom = Color(0xFF1A1F2E),
    textPrimary = Color.White,
    textSecondary = Color(0xFF9CA3AF),
    textDim = Color(0xFF6B7280),
    accentGreen = Color(0xFF22C55E),
    accentBlue = Color(0xFF3B82F6),
    accentRed = Color(0xFFEF4444),
    glassSurface = Color(0xFF2A2D35),
    glassHighlight = Color(0xFF4A4D55),
    glassBorder = Color.White.copy(alpha = 0.12f),
    cardBackground = Color(0xFF2A2D35),
    cardShadow = Color.Black.copy(alpha = 0.3f),
    navIndicator = Color(0xFF22C55E).copy(alpha = 0.20f),
)

fun colorSchemeFor(isDark: Boolean): AppColorScheme =
    if (isDark) DarkColorScheme else LightColorScheme
