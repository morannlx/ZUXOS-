package me.inkdye.zuxos.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import me.inkdye.zuxos.ui.theme.ThemeMode

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
        prefs(context).getBoolean(KEY_LIQUID_GLASS, false)

    fun setLiquidGlassEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_LIQUID_GLASS, enabled).apply()
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

    /** Called on each ON_RESUME to catch preference changes made in other activities. */
    fun syncFromPreferences() {
        themeMode.value = AppPreferences.getThemeMode(context)
        floatingNavEnabled.value = AppPreferences.isFloatingNavEnabled(context)
        liquidGlassEnabled.value = AppPreferences.isLiquidGlassEnabled(context)
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
