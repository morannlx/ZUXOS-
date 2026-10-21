package me.inkdye.zuxos.hook.lenovo

import android.graphics.Color
import android.util.Log
import android.view.View
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import kotlin.math.roundToInt

object LenovoSystemUIControlCenterHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_ENABLED
    override val targetPackage = PACKAGE_SYSTEMUI
    override val moduleName = "lenovo_systemui_control_center"

    private const val TAG = "ZUXOS.LenovoQS"
    private const val PACKAGE_SYSTEMUI = "com.android.systemui"
    private const val QS_ACTIVE = 2
    private const val QS_INACTIVE = 1
    private const val DEFAULT_ACTIVE_COLOR = 0xFF5FAE8B.toInt()
    private const val DEFAULT_INACTIVE_COLOR = 0xFFFFFFFF.toInt()

    override fun PackageParam.onHook() {
        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).apply { reload() }
        val config = ControlCenterConfig.from(prefs)
        val classLoader = hostClassLoader

        hookTileBackgroundColors(classLoader, config)
        hookQsContainers(classLoader, config)
        hookCrossWindowBackgroundBlur(classLoader, config)
    }

    private fun hookTileBackgroundColors(classLoader: ClassLoader?, config: ControlCenterConfig) {
        QSTILE_VIEW_IMPL_CANDIDATES.forEachClass(classLoader) { clazz ->
            XposedHelpers.findAndHookMethod(
                clazz,
                "getBackgroundColorForState",
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val state = param.args.getOrNull(0) as? Int ?: return
                        val disabledByPolicy = param.args.getOrNull(2) as? Boolean ?: false
                        if (disabledByPolicy || state !in setOf(QS_ACTIVE, QS_INACTIVE)) return

                        val alpha = if (state == QS_ACTIVE) config.tileActiveAlpha else config.tileInactiveAlpha
                        val baseColor = if (state == QS_ACTIVE) config.tileActiveColor else config.tileInactiveColor
                        param.result = baseColor.withAlpha(alpha)
                    }
                },
            )
            Log.i(TAG, "Hooked tile background color: ${clazz.name}")
        }
    }

    private fun hookQsContainers(classLoader: ClassLoader?, config: ControlCenterConfig) {
        QS_IMPL_CANDIDATES.forEachClass(classLoader) { clazz ->
            val componentClassName = if (clazz.name.contains(".p037qs.")) {
                "com.android.systemui.p037qs.dagger.QSComponent"
            } else {
                "com.android.systemui.qs.dagger.QSComponent"
            }
            XposedHelpers.findAndHookMethod(
                clazz,
                "onComponentCreated",
                XposedHelpers.findClassIfExists(componentClassName, classLoader) ?: return@forEachClass,
                android.os.Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val qsImpl = param.thisObject ?: return
                        qsImpl.applyControlCenterAlpha(config.panelAlpha)
                    }
                },
            )

            XposedHelpers.findAndHookMethod(
                clazz,
                "setQSCustomizerBehindAlpha",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val qsImpl = param.thisObject ?: return
                        qsImpl.applyControlCenterAlpha(config.panelAlpha)
                    }
                },
            )
            Log.i(TAG, "Hooked QS container alpha: ${clazz.name}")
        }
    }

    private fun hookCrossWindowBackgroundBlur(classLoader: ClassLoader?, config: ControlCenterConfig) {
        val viewRootImplClass = XposedHelpers.findClassIfExists("android.view.ViewRootImpl", null) ?: return
        BLUR_UTILS_CANDIDATES.forEachClass(classLoader) { clazz ->
            XposedHelpers.findAndHookMethod(
                clazz,
                "applyBlur",
                viewRootImplClass,
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val viewRootImpl = param.args.getOrNull(0) ?: return
                        if (!viewRootImpl.isNotificationShadeViewRoot()) return
                        val originalRadius = param.args.getOrNull(1) as? Int ?: return
                        if (originalRadius <= 0) return
                        val systemMaxRadius = (param.thisObject?.floatFieldOrNull("maxBlurRadius") ?: 400f)
                            .coerceAtLeast(1f)
                        val configuredMaxRadius = config.pullDownBlurRadius.coerceIn(0f, 200f)
                        val scaledRadius = (originalRadius * (configuredMaxRadius / systemMaxRadius)).roundToInt()
                        param.args[1] = if (scaledRadius == 0 && configuredMaxRadius > 0f) 1 else scaledRadius
                    }
                },
            )
            Log.i(TAG, "Hooked cross-window shade background blur: ${clazz.name}")
        }
    }

    private fun Any.applyControlCenterAlpha(panelAlpha: Float) {
        val alpha = panelAlpha.coerceIn(0f, 1f)
        val rootView = fieldOrNull("mRootView") as? View
        if (rootView != null) {
            rootView.alpha = alpha
            return
        }
        listOf(
            fieldOrNull("mQSPanelScrollView"),
            fieldOrNull("mHeaderQSPanelScrollView"),
            fieldOrNull("mHeaderQQSPanelContainer"),
            fieldOrNull("mQSCustomFooter"),
            fieldOrNull("mFooterActionsView"),
        ).filterIsInstance<View>().forEach { it.alpha = alpha }
    }

    private fun Array<String>.forEachClass(classLoader: ClassLoader?, block: (Class<*>) -> Unit) {
        forEach { className ->
            val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            runCatching { block(clazz) }.onFailure { Log.w(TAG, "Hook failed for $className", it) }
        }
    }

    private fun Any.fieldOrNull(name: String): Any? =
        runCatching { XposedHelpers.getObjectField(this, name) }.getOrNull()

    private fun Any.floatFieldOrNull(name: String): Float? =
        runCatching { XposedHelpers.getFloatField(this, name) }.getOrNull()

    private fun Any.isNotificationShadeViewRoot(): Boolean {
        val rootView = runCatching { XposedHelpers.callMethod(this, "getView") as? View }.getOrNull()
        val rootViewName = rootView?.javaClass?.name.orEmpty()
        return rootViewName == "com.android.systemui.shade.NotificationShadeWindowView" ||
            rootViewName == "com.android.systemui.scene.ui.view.WindowRootView"
    }

    private fun Int.withAlpha(alpha: Float): Int =
        Color.argb((alpha.coerceIn(0f, 1f) * 255f).roundToInt(), Color.red(this), Color.green(this), Color.blue(this))

    private data class ControlCenterConfig(
        val tileInactiveColor: Int,
        val tileActiveColor: Int,
        val tileInactiveAlpha: Float,
        val tileActiveAlpha: Float,
        val panelAlpha: Float,
        val pullDownBlurRadius: Float,
    ) {
        companion object {
            fun from(prefs: XSharedPreferences): ControlCenterConfig = ControlCenterConfig(
                tileInactiveColor = prefs.getInt(
                    ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR,
                    DEFAULT_INACTIVE_COLOR,
                ),
                tileActiveColor = prefs.getInt(
                    ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR,
                    DEFAULT_ACTIVE_COLOR,
                ),
                tileInactiveAlpha = prefs.getFloat(
                    ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA,
                    0.35f,
                ),
                tileActiveAlpha = prefs.getFloat(
                    ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA,
                    0.92f,
                ),
                panelAlpha = prefs.getFloat(
                    ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA,
                    0.9f,
                ),
                pullDownBlurRadius = prefs.getFloat(
                    ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS,
                    18f,
                ),
            )
        }
    }

    private val QS_IMPL_CANDIDATES = arrayOf(
        "com.android.systemui.qs.QSImpl",
        "com.android.systemui.p037qs.QSImpl",
    )

    private val QSTILE_VIEW_IMPL_CANDIDATES = arrayOf(
        "com.android.systemui.qs.tileimpl.QSTileViewImpl",
        "com.android.systemui.p037qs.tileimpl.QSTileViewImpl",
        "com.android.systemui.qs.tileimpl.PcModeQSTileViewImpl",
        "com.android.systemui.p037qs.tileimpl.PcModeQSTileViewImpl",
    )

    private val BLUR_UTILS_CANDIDATES = arrayOf(
        "com.android.systemui.statusbar.BlurUtils",
    )
}
