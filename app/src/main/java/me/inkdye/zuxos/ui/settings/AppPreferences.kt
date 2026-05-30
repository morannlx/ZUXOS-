package me.inkdye.zuxos.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.inkdye.zuxos.ui.theme.ThemeMode
import java.io.File

/**
 * 应用偏好设置管理器
 *
 * 使用 SharedPreferences 持久化存储用户偏好，支持：
 * - 主题模式（浅色/深色/跟随系统）
 * - 悬浮导航栏开关
 * - 液态玻璃效果开关
 */
object AppPreferences {
    private const val PREFS_NAME = "zuxos_app_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FLOATING_NAV = "floating_nav_enabled"
    private const val KEY_LIQUID_GLASS = "liquid_glass_enabled"
    private const val KEY_GLASS_CARD_OPACITY = "glass_card_opacity"
    private const val KEY_TITLE_TEXT_CONTRAST = "title_text_contrast"
    private const val KEY_DESCRIPTION_TEXT_CONTRAST = "description_text_contrast"
    private const val KEY_CUSTOM_BACKGROUND_ENABLED = "custom_background_enabled"
    private const val KEY_CUSTOM_BACKGROUND_VERSION = "custom_background_version"
    private const val CUSTOM_BACKGROUND_FILE_NAME = "custom_app_background"
    const val DEFAULT_GLASS_CARD_OPACITY = 0.34f
    const val MIN_GLASS_CARD_OPACITY = 0.18f
    const val MAX_GLASS_CARD_OPACITY = 0.72f
    const val DEFAULT_TEXT_CONTRAST = 1.0f
    const val MIN_TEXT_CONTRAST = 0.75f
    const val MAX_TEXT_CONTRAST = 1.35f

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Theme Mode ──────────────────────────────────
    fun getThemeMode(context: Context): ThemeMode =
        ThemeMode.fromOrdinal(prefs(context).getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.ordinal))

    fun setThemeMode(context: Context, mode: ThemeMode) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode.ordinal).apply()
    }

    // ── Floating Navigation ─────────────────────────
    fun isFloatingNavEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FLOATING_NAV, false)

    fun setFloatingNavEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_FLOATING_NAV, enabled).apply()
    }

    // ── Liquid Glass Effect ─────────────────────────
    fun isLiquidGlassEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LIQUID_GLASS, true)

    fun setLiquidGlassEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_LIQUID_GLASS, enabled).apply()
    }

    fun getGlassCardOpacity(context: Context): Float =
        prefs(context)
            .getFloat(KEY_GLASS_CARD_OPACITY, DEFAULT_GLASS_CARD_OPACITY)
            .coerceIn(MIN_GLASS_CARD_OPACITY, MAX_GLASS_CARD_OPACITY)

    fun setGlassCardOpacity(context: Context, opacity: Float) {
        prefs(context)
            .edit()
            .putFloat(KEY_GLASS_CARD_OPACITY, opacity.coerceIn(MIN_GLASS_CARD_OPACITY, MAX_GLASS_CARD_OPACITY))
            .apply()
    }

    fun getTitleTextContrast(context: Context): Float =
        prefs(context)
            .getFloat(KEY_TITLE_TEXT_CONTRAST, DEFAULT_TEXT_CONTRAST)
            .coerceIn(MIN_TEXT_CONTRAST, MAX_TEXT_CONTRAST)

    fun setTitleTextContrast(context: Context, contrast: Float) {
        prefs(context)
            .edit()
            .putFloat(KEY_TITLE_TEXT_CONTRAST, contrast.coerceIn(MIN_TEXT_CONTRAST, MAX_TEXT_CONTRAST))
            .apply()
    }

    fun getDescriptionTextContrast(context: Context): Float =
        prefs(context)
            .getFloat(KEY_DESCRIPTION_TEXT_CONTRAST, DEFAULT_TEXT_CONTRAST)
            .coerceIn(MIN_TEXT_CONTRAST, MAX_TEXT_CONTRAST)

    fun setDescriptionTextContrast(context: Context, contrast: Float) {
        prefs(context)
            .edit()
            .putFloat(KEY_DESCRIPTION_TEXT_CONTRAST, contrast.coerceIn(MIN_TEXT_CONTRAST, MAX_TEXT_CONTRAST))
            .apply()
    }

    // ── Custom App Background ───────────────────────
    fun isCustomBackgroundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CUSTOM_BACKGROUND_ENABLED, false)

    fun setCustomBackgroundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_CUSTOM_BACKGROUND_ENABLED, enabled).apply()
    }

    fun customBackgroundFile(context: Context): File =
        File(context.filesDir, CUSTOM_BACKGROUND_FILE_NAME)

    fun hasCustomBackground(context: Context): Boolean =
        customBackgroundFile(context).isFile

    fun getCustomBackgroundVersion(context: Context): Int =
        prefs(context).getInt(KEY_CUSTOM_BACKGROUND_VERSION, 0)

    fun bumpCustomBackgroundVersion(context: Context) {
        val nextVersion = getCustomBackgroundVersion(context) + 1
        prefs(context).edit().putInt(KEY_CUSTOM_BACKGROUND_VERSION, nextVersion).apply()
    }

    fun clearCustomBackground(context: Context) {
        customBackgroundFile(context).delete()
        bumpCustomBackgroundVersion(context)
    }
}

/**
 * Composable 状态包装器，将 SharedPreferences 值包装为 Compose 可观察状态
 */
class AppSettingsState(private val context: Context) {
    val themeMode: MutableState<ThemeMode> =
        mutableStateOf(AppPreferences.getThemeMode(context))
    val floatingNavEnabled: MutableState<Boolean> =
        mutableStateOf(AppPreferences.isFloatingNavEnabled(context))
    val liquidGlassEnabled: MutableState<Boolean> =
        mutableStateOf(AppPreferences.isLiquidGlassEnabled(context))
    val glassCardOpacity: MutableState<Float> =
        mutableStateOf(AppPreferences.getGlassCardOpacity(context))
    val titleTextContrast: MutableState<Float> =
        mutableStateOf(AppPreferences.getTitleTextContrast(context))
    val descriptionTextContrast: MutableState<Float> =
        mutableStateOf(AppPreferences.getDescriptionTextContrast(context))
    val customBackgroundEnabled: MutableState<Boolean> =
        mutableStateOf(AppPreferences.isCustomBackgroundEnabled(context))
    val customBackgroundVersion: MutableState<Int> =
        mutableStateOf(AppPreferences.getCustomBackgroundVersion(context))

    /** Called on each ON_RESUME to catch preference changes made in other activities. */
    fun syncFromPreferences() {
        themeMode.value = AppPreferences.getThemeMode(context)
        floatingNavEnabled.value = AppPreferences.isFloatingNavEnabled(context)
        liquidGlassEnabled.value = AppPreferences.isLiquidGlassEnabled(context)
        glassCardOpacity.value = AppPreferences.getGlassCardOpacity(context)
        titleTextContrast.value = AppPreferences.getTitleTextContrast(context)
        descriptionTextContrast.value = AppPreferences.getDescriptionTextContrast(context)
        customBackgroundEnabled.value = AppPreferences.isCustomBackgroundEnabled(context)
        customBackgroundVersion.value = AppPreferences.getCustomBackgroundVersion(context)
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode.value = mode
        AppPreferences.setThemeMode(context, mode)
    }

    fun updateFloatingNav(enabled: Boolean) {
        floatingNavEnabled.value = enabled
        AppPreferences.setFloatingNavEnabled(context, enabled)
    }

    fun updateLiquidGlass(enabled: Boolean) {
        liquidGlassEnabled.value = enabled
        AppPreferences.setLiquidGlassEnabled(context, enabled)
    }

    fun updateGlassCardOpacity(opacity: Float) {
        val nextOpacity = opacity.coerceIn(AppPreferences.MIN_GLASS_CARD_OPACITY, AppPreferences.MAX_GLASS_CARD_OPACITY)
        glassCardOpacity.value = nextOpacity
        AppPreferences.setGlassCardOpacity(context, nextOpacity)
    }

    fun updateTitleTextContrast(contrast: Float) {
        val nextContrast = contrast.coerceIn(AppPreferences.MIN_TEXT_CONTRAST, AppPreferences.MAX_TEXT_CONTRAST)
        titleTextContrast.value = nextContrast
        AppPreferences.setTitleTextContrast(context, nextContrast)
    }

    fun updateDescriptionTextContrast(contrast: Float) {
        val nextContrast = contrast.coerceIn(AppPreferences.MIN_TEXT_CONTRAST, AppPreferences.MAX_TEXT_CONTRAST)
        descriptionTextContrast.value = nextContrast
        AppPreferences.setDescriptionTextContrast(context, nextContrast)
    }

    fun updateCustomBackgroundEnabled(enabled: Boolean) {
        customBackgroundEnabled.value = enabled
        AppPreferences.setCustomBackgroundEnabled(context, enabled)
    }

    fun notifyCustomBackgroundChanged() {
        AppPreferences.bumpCustomBackgroundVersion(context)
        customBackgroundVersion.value = AppPreferences.getCustomBackgroundVersion(context)
    }

    fun clearCustomBackground() {
        AppPreferences.clearCustomBackground(context)
        customBackgroundVersion.value = AppPreferences.getCustomBackgroundVersion(context)
    }
}

/**
 * 记住 [AppSettingsState] 实例，在 Activity 恢复时自动同步 SharedPreferences。
 */
@Composable
fun rememberAppSettings(): AppSettingsState {
    val context = LocalContext.current
    val state = remember { AppSettingsState(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state.syncFromPreferences()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return state
}
