package me.inkdye.zuxos.hook.lenovo

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook

object LenovoDesktopModeRotationHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_DESKTOP_MODE_ROTATION_MODE
    override val alwaysRegister = true
    override val targetPackages = setOf(PACKAGE_ANDROID, PACKAGE_SYSTEMUI, PACKAGE_SETTINGS)
    override val moduleName = "lenovo_desktop_mode_rotation"

    private const val TAG = "ZUXOS.DesktopRotation"
    private const val PACKAGE_ANDROID = "android"
    private const val PACKAGE_SYSTEMUI = "com.android.systemui"
    private const val PACKAGE_SETTINGS = "com.android.settings"
    private const val KEY_PC_MODE = "zui_pc_mode"
    private const val KEY_DP_PC_MODE = "zui_dp_display_pc_mode"
    private const val KEY_SCREEN_ORIENTATION_VALUE = "screen_orientation_value"
    private const val QS_ACTIVE = 2
    private const val QS_INACTIVE = 1
    private const val AVAILABILITY_AVAILABLE = 0
    private const val AVAILABILITY_CONDITIONALLY_UNAVAILABLE = 5
    private const val FIXED_TO_USER_ROTATION_DEFAULT = 0
    private const val FIXED_TO_USER_ROTATION_ENABLED = 2
    private const val MODE_AUTO = 0
    private const val MODE_LANDSCAPE = 1
    private const val MODE_REVERSE_LANDSCAPE = 2
    private const val MODE_PORTRAIT = 3
    private const val MODE_REVERSE_PORTRAIT = 4

    private val originalPcMode = ThreadLocal<Boolean?>()

    @Volatile
    private var naturalLandscapeCache: Boolean? = null

    private val frozenPcDisplayIds = linkedSetOf<Int>()

    override fun PackageParam.onHook() {
        hookSystemRotationSettingsAccess()
        when (packageName) {
            PACKAGE_ANDROID -> hookFrameworkDisplayRotation(hostClassLoader)
            PACKAGE_SYSTEMUI -> {
                installRotationSettingsWriter()
                hookPcModeManagerDisplayRotation(hostClassLoader)
                hookPcModeCheckForOrientationHandler(hostClassLoader)
                hookDesktopActivityOrientationChangeHandler(hostClassLoader)
                hookRotationLockTile(hostClassLoader)
                hookDpModeAnimationWindow(hostClassLoader)
            }

            PACKAGE_SETTINGS -> {
                installRotationSettingsWriter()
                hookSettingsRotationControllers(hostClassLoader)
            }
        }
    }

    private fun installRotationSettingsWriter() {
        runCatching {
            XposedHelpers.findAndHookMethod(
                Application::class.java,
                "attach",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val context = param.args.firstOrNull() as? Context ?: return
                        naturalLandscapeCache = context.isNaturalLandscape()
                        applyRotationModeIfPcMode(context.contentResolver)
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook Application.attach failed", it) }
    }

    private fun hookSystemRotationSettingsAccess() {
        runCatching {
            hookSystemRotationGetInt("getInt", hasDefault = true)
            hookSystemRotationGetInt("getInt", hasDefault = false)
            hookSystemRotationGetIntForUser(hasDefault = true)
            hookSystemRotationGetIntForUser(hasDefault = false)
            hookSystemRotationSettingWrites()
            Log.i(TAG, "Hooked Settings.System rotation reads/writes")
        }.onFailure { Log.w(TAG, "Hook Settings.System rotation access failed", it) }
    }

    private fun hookSystemRotationGetInt(methodName: String, hasDefault: Boolean) {
        val parameterTypes = if (hasDefault) {
            arrayOf(ContentResolver::class.java, String::class.java, Int::class.javaPrimitiveType)
        } else {
            arrayOf(ContentResolver::class.java, String::class.java)
        }
        runCatching {
            XposedHelpers.findAndHookMethod(
                Settings.System::class.java,
                methodName,
                *parameterTypes,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val resolver = param.args.firstOrNull() as? ContentResolver ?: return
                        val key = param.args.getOrNull(1) as? String ?: return
                        if (key == KEY_SCREEN_ORIENTATION_VALUE && resolver.isPcOrDpMode()) {
                            param.result = 0
                        }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook Settings.System.$methodName failed", it) }
    }

    private fun hookSystemRotationGetIntForUser(hasDefault: Boolean) {
        val parameterTypes = if (hasDefault) {
            arrayOf(
                ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
            )
        } else {
            arrayOf(ContentResolver::class.java, String::class.java, Int::class.javaPrimitiveType)
        }
        runCatching {
            XposedHelpers.findAndHookMethod(
                Settings.System::class.java,
                "getIntForUser",
                *parameterTypes,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val resolver = param.args.firstOrNull() as? ContentResolver ?: return
                        val key = param.args.getOrNull(1) as? String ?: return
                        if (key == KEY_SCREEN_ORIENTATION_VALUE && resolver.isPcOrDpMode()) {
                            param.result = 0
                        }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook Settings.System.getIntForUser failed", it) }
    }

    private fun hookSystemRotationSettingWrites() {
        runCatching {
            XposedHelpers.findAndHookMethod(
                Settings.System::class.java,
                "putInt",
                ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        patchRotationSettingWrite(param.args, valueIndex = 2)
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        applyRotationModeAfterPcModeEnabled(param.args)
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook Settings.System.putInt failed", it) }
        runCatching {
            XposedHelpers.findAndHookMethod(
                Settings.System::class.java,
                "putIntForUser",
                ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        patchRotationSettingWrite(param.args, valueIndex = 2)
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        applyRotationModeAfterPcModeEnabled(param.args)
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook Settings.System.putIntForUser failed", it) }
    }

    private fun patchRotationSettingWrite(args: Array<Any?>, valueIndex: Int) {
        val key = args.getOrNull(1) as? String ?: return
        val resolver = args.firstOrNull() as? ContentResolver ?: return
        if (!resolver.isPcOrDpMode()) return
        val mode = configuredRotationMode()
        when (key) {
            KEY_SCREEN_ORIENTATION_VALUE -> args[valueIndex] = 0
            Settings.System.ACCELEROMETER_ROTATION -> if (mode == MODE_AUTO) args[valueIndex] = 1
        }
    }

    private fun applyRotationModeAfterPcModeEnabled(args: Array<Any?>) {
        val resolver = args.firstOrNull() as? ContentResolver ?: return
        val key = args.getOrNull(1) as? String ?: return
        val value = args.getOrNull(2) as? Int ?: return
        if ((key == KEY_PC_MODE || key == KEY_DP_PC_MODE) && value == 1) {
            applyRotationModeIfPcMode(resolver)
        }
    }

    private fun hookPcModeManagerDisplayRotation(classLoader: ClassLoader?) {
        val pcModeManagerClass = XposedHelpers.findClassIfExists(
            "com.android.systemui.pcmode.PcModeManager",
            classLoader,
        ) ?: run {
            Log.w(TAG, "PcModeManager not found")
            return
        }

        runCatching {
            XposedBridge.hookAllMethods(
                pcModeManagerClass,
                "addNavBarFm",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val display = param.args.getOrNull(1) as? Display ?: return
                        val context = param.thisObject.contextFromPcModeManager() ?: return
                        applyPcDisplayRotation(context, param.thisObject, setOf(display.displayId))
                    }
                },
            )
            XposedBridge.hookAllMethods(
                pcModeManagerClass,
                "enterWorkMode",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject.schedulePcDisplayRotation()
                    }
                },
            )
            XposedBridge.hookAllMethods(
                pcModeManagerClass,
                "switchWorkMode",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val context = param.thisObject.contextFromPcModeManager() ?: return
                        if (context.isPcOrDpMode()) {
                            param.thisObject.schedulePcDisplayRotation()
                        } else {
                            releaseFrozenPcDisplays()
                        }
                    }
                },
            )
            XposedBridge.hookAllMethods(
                pcModeManagerClass,
                "exitPcMode",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        releaseFrozenPcDisplays()
                    }
                },
            )
            Log.i(TAG, "Hooked PcModeManager display rotation")
        }.onFailure { Log.w(TAG, "Hook PcModeManager display rotation failed", it) }
    }

    private fun hookFrameworkDisplayRotation(classLoader: ClassLoader?) {
        val displayRotationClass = XposedHelpers.findClassIfExists(
            "com.android.server.wm.DisplayRotation",
            classLoader,
        ) ?: run {
            Log.w(TAG, "DisplayRotation not found")
            return
        }

        runCatching {
            XposedBridge.hookAllMethods(
                displayRotationClass,
                "rotationForOrientation",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val context = param.thisObject.contextFromDisplayRotation() ?: return
                        if (!param.thisObject.shouldForceFrameworkDisplayRotation(context)) return
                        val rotation = configuredUserRotationOrNull(context) ?: return
                        param.result = rotation
                    }
                },
            )
            XposedBridge.hookAllMethods(
                displayRotationClass,
                "setUserRotation",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val context = param.thisObject.contextFromDisplayRotation() ?: return
                        if (!param.thisObject.shouldForceFrameworkDisplayRotation(context)) return
                        val rotation = configuredUserRotationOrNull(context) ?: return
                        param.args[0] = 1
                        param.args[1] = rotation
                    }
                },
            )
            Log.i(TAG, "Hooked framework DisplayRotation for PC mode")
        }.onFailure { Log.w(TAG, "Hook framework DisplayRotation failed", it) }
    }

    private fun hookDesktopActivityOrientationChangeHandler(classLoader: ClassLoader?) {
        val handlerClass = findFirstClass(
            classLoader,
            "com.android.wm.shell.desktopmode.DesktopActivityOrientationChangeHandler",
            "com.android.p033wm.shell.desktopmode.DesktopActivityOrientationChangeHandler",
            "com.android.p133wm.shell.desktopmode.DesktopActivityOrientationChangeHandler",
        ) ?: run {
            Log.w(TAG, "DesktopActivityOrientationChangeHandler not found")
            return
        }

        runCatching {
            XposedBridge.hookAllMethods(
                handlerClass,
                "handleActivityOrientationChange",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val pcModeUtilsClass = findFirstClass(
                                classLoader,
                                "com.android.wm.shell.ov.utils.OvcPcModeUtils",
                                "com.android.p033wm.shell.p034ov.utils.OvcPcModeUtils",
                                "com.android.p133wm.shell.p134ov.utils.OvcPcModeUtils",
                            ) ?: return
                            val original = XposedHelpers.getStaticBooleanField(pcModeUtilsClass, "mIsInPcMode")
                            originalPcMode.set(original)
                            if (original) {
                                XposedHelpers.setStaticBooleanField(pcModeUtilsClass, "mIsInPcMode", false)
                            }
                        }.onFailure { Log.w(TAG, "Disable PC-mode orientation guard failed", it) }
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val original = originalPcMode.get() ?: return
                            val pcModeUtilsClass = findFirstClass(
                                classLoader,
                                "com.android.wm.shell.ov.utils.OvcPcModeUtils",
                                "com.android.p033wm.shell.p034ov.utils.OvcPcModeUtils",
                                "com.android.p133wm.shell.p134ov.utils.OvcPcModeUtils",
                            ) ?: return
                            XposedHelpers.setStaticBooleanField(pcModeUtilsClass, "mIsInPcMode", original)
                        }.onFailure { Log.w(TAG, "Restore PC-mode orientation guard failed", it) }
                        originalPcMode.remove()
                    }
                },
            )
            Log.i(TAG, "Hooked DesktopActivityOrientationChangeHandler")
        }.onFailure { Log.w(TAG, "Hook DesktopActivityOrientationChangeHandler failed", it) }
    }

    private fun hookPcModeCheckForOrientationHandler(classLoader: ClassLoader?) {
        val pcModeUtilsClass = findFirstClass(
            classLoader,
            "com.android.wm.shell.ov.utils.OvcPcModeUtils",
            "com.android.p033wm.shell.p034ov.utils.OvcPcModeUtils",
            "com.android.p133wm.shell.p134ov.utils.OvcPcModeUtils",
        ) ?: run {
            Log.w(TAG, "OvcPcModeUtils not found")
            return
        }

        runCatching {
            XposedHelpers.findAndHookMethod(
                pcModeUtilsClass,
                "isUnderPcMode",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isCalledFromDesktopOrientationHandler()) {
                            param.result = false
                        }
                    }
                },
            )
            Log.i(TAG, "Hooked OvcPcModeUtils.isUnderPcMode for orientation handler")
        }.onFailure { Log.w(TAG, "Hook OvcPcModeUtils.isUnderPcMode failed", it) }
    }

    private fun hookRotationLockTile(classLoader: ClassLoader?) {
        val tileClass = findFirstClass(
            classLoader,
            "com.android.systemui.qs.tiles.RotationLockTile",
            "com.android.systemui.p037qs.tiles.RotationLockTile",
        ) ?: run {
            Log.w(TAG, "RotationLockTile not found")
            return
        }

        runCatching {
            XposedBridge.hookAllMethods(
                tileClass,
                "handleUpdateState",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val state = param.args.firstOrNull() ?: return
                        runCatching {
                            val currentState = XposedHelpers.getIntField(state, "state")
                            if (currentState != 0) return
                            val enabled = runCatching { XposedHelpers.getBooleanField(state, "value") }
                                .getOrDefault(false)
                            XposedHelpers.setIntField(state, "state", if (enabled) QS_ACTIVE else QS_INACTIVE)
                            runCatching { XposedHelpers.setFloatField(state, "iconAlpha", 1f) }
                        }.onFailure { Log.w(TAG, "Patch RotationLockTile state failed", it) }
                    }
                },
            )
            Log.i(TAG, "Hooked RotationLockTile")
        }.onFailure { Log.w(TAG, "Hook RotationLockTile failed", it) }
    }

    private fun hookDpModeAnimationWindow(classLoader: ClassLoader?) {
        val animClass = XposedHelpers.findClassIfExists(
            "com.android.systemui.dpmode.EnterDpModeAnimWin",
            classLoader,
        ) ?: run {
            Log.w(TAG, "EnterDpModeAnimWin not found")
            return
        }

        runCatching {
            XposedBridge.hookAllConstructors(
                animClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val layoutParams = param.thisObject.fieldOrNull("mLayoutParams")
                                ?: param.thisObject.fieldOrNull("layoutParams")
                                ?: return
                            XposedHelpers.setIntField(layoutParams, "screenOrientation", configuredActivityOrientation())
                        }.onFailure { Log.w(TAG, "Patch DP mode animation orientation failed", it) }
                    }
                },
            )
            Log.i(TAG, "Hooked EnterDpModeAnimWin")
        }.onFailure { Log.w(TAG, "Hook EnterDpModeAnimWin failed", it) }
    }

    private fun hookSettingsRotationControllers(classLoader: ClassLoader?) {
        hookAutoRotatePreferenceController(classLoader)
        hookScreenRotatePreferenceController(classLoader)
    }

    private fun hookAutoRotatePreferenceController(classLoader: ClassLoader?) {
        val controllerClass = XposedHelpers.findClassIfExists(
            "com.android.settings.display.AutoRotatePreferenceController",
            classLoader,
        ) ?: run {
            Log.w(TAG, "AutoRotatePreferenceController not found")
            return
        }

        runCatching {
            XposedHelpers.findAndHookMethod(
                controllerClass,
                "getAvailabilityStatus",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val status = param.result as? Int ?: return
                        if (status == AVAILABILITY_CONDITIONALLY_UNAVAILABLE) {
                            param.result = AVAILABILITY_AVAILABLE
                        }
                    }
                },
            )
            Log.i(TAG, "Hooked AutoRotatePreferenceController")
        }.onFailure { Log.w(TAG, "Hook AutoRotatePreferenceController failed", it) }
    }

    private fun hookScreenRotatePreferenceController(classLoader: ClassLoader?) {
        val controllerClass = XposedHelpers.findClassIfExists(
            "com.android.settings.display.ScreenRotatePreferenceController",
            classLoader,
        ) ?: run {
            Log.w(TAG, "ScreenRotatePreferenceController not found")
            return
        }

        runCatching {
            XposedHelpers.findAndHookMethod(
                controllerClass,
                "isAvailable",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (param.result == false && param.thisObject?.isPcModeController() == true) {
                            param.result = true
                        }
                    }
                },
            )
            Log.i(TAG, "Hooked ScreenRotatePreferenceController")
        }.onFailure { Log.w(TAG, "Hook ScreenRotatePreferenceController failed", it) }
    }

    private fun Any.schedulePcDisplayRotation() {
        val context = contextFromPcModeManager() ?: return
        val handler = Handler(Looper.getMainLooper())
        handler.post { applyPcDisplayRotation(context, this) }
        handler.postDelayed({ applyPcDisplayRotation(context, this) }, 500L)
        handler.postDelayed({ applyPcDisplayRotation(context, this) }, 1500L)
    }

    private fun applyPcDisplayRotation(
        context: Context,
        pcModeManager: Any? = null,
        forcedDisplayIds: Set<Int> = emptySet(),
    ) {
        if (!context.isPcOrDpMode()) return
        applyRotationModeIfPcMode(context.contentResolver)
        val displayIds = forcedDisplayIds.ifEmpty {
            pcModeManager.displayIdsFromPcModeManager().ifEmpty { context.pcModeCandidateDisplayIds() }
        }
        if (displayIds.isEmpty()) return

        val rotation = configuredUserRotationOrNull(context)
        if (rotation == null) {
            thawPcDisplays(displayIds)
            return
        }
        displayIds.forEach { displayId ->
            freezePcDisplay(displayId, rotation)
        }
    }

    private fun freezePcDisplay(displayId: Int, rotation: Int) {
        if (displayId == Display.INVALID_DISPLAY) return
        val windowManager = windowManagerServiceOrNull() ?: return
        runCatching {
            XposedHelpers.callMethod(
                windowManager,
                "freezeDisplayRotation",
                displayId,
                rotation,
                TAG,
            )
            XposedHelpers.callMethod(
                windowManager,
                "setFixedToUserRotation",
                displayId,
                FIXED_TO_USER_ROTATION_ENABLED,
            )
            XposedHelpers.callMethod(windowManager, "setIgnoreOrientationRequest", displayId, false)
            synchronized(frozenPcDisplayIds) {
                frozenPcDisplayIds.add(displayId)
            }
            Log.i(TAG, "Frozen PC display rotation displayId=$displayId rotation=$rotation")
        }.onFailure { Log.w(TAG, "Freeze PC display rotation failed displayId=$displayId", it) }
    }

    private fun releaseFrozenPcDisplays() {
        val displayIds = synchronized(frozenPcDisplayIds) {
            frozenPcDisplayIds.toList().also { frozenPcDisplayIds.clear() }
        }
        thawPcDisplays(displayIds)
    }

    private fun thawPcDisplays(displayIds: Collection<Int>) {
        val windowManager = windowManagerServiceOrNull() ?: return
        displayIds.filter { it != Display.INVALID_DISPLAY }.distinct().forEach { displayId ->
            runCatching {
                XposedHelpers.callMethod(
                    windowManager,
                    "setFixedToUserRotation",
                    displayId,
                    FIXED_TO_USER_ROTATION_DEFAULT,
                )
                XposedHelpers.callMethod(windowManager, "thawDisplayRotation", displayId, TAG)
                synchronized(frozenPcDisplayIds) {
                    frozenPcDisplayIds.remove(displayId)
                }
                Log.i(TAG, "Thawed PC display rotation displayId=$displayId")
            }.onFailure { Log.w(TAG, "Thaw PC display rotation failed displayId=$displayId", it) }
        }
    }

    private fun applyRotationModeIfPcMode(resolver: ContentResolver) {
        if (!resolver.isPcOrDpMode()) return
        runCatching {
            Settings.System.putInt(resolver, KEY_SCREEN_ORIENTATION_VALUE, 0)
        }.onFailure { Log.w(TAG, "Apply desktop mode rotation settings failed", it) }
    }

    private fun configuredRotationMode(): Int =
        ModuleConfig.readInt(ModuleConfig.LENOVO_DESKTOP_MODE_ROTATION_MODE, MODE_AUTO)
            .coerceIn(MODE_AUTO, MODE_REVERSE_PORTRAIT)

    private fun configuredUserRotationOrNull(context: Context): Int? {
        val naturalLandscape = naturalLandscapeCache ?: context.isNaturalLandscape()
        naturalLandscapeCache = naturalLandscape
        return configuredRotationMode().userRotationOrNull(naturalLandscape)
    }

    private fun configuredActivityOrientation(): Int = when (configuredRotationMode()) {
        MODE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        MODE_REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        MODE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        MODE_REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        else -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    private fun Int.userRotationOrNull(naturalLandscape: Boolean): Int? {
        return if (naturalLandscape) {
            when (this) {
                MODE_LANDSCAPE -> Surface.ROTATION_0
                MODE_REVERSE_LANDSCAPE -> Surface.ROTATION_180
                MODE_PORTRAIT -> Surface.ROTATION_90
                MODE_REVERSE_PORTRAIT -> Surface.ROTATION_270
                else -> null
            }
        } else {
            when (this) {
                MODE_LANDSCAPE -> Surface.ROTATION_90
                MODE_REVERSE_LANDSCAPE -> Surface.ROTATION_270
                MODE_PORTRAIT -> Surface.ROTATION_0
                MODE_REVERSE_PORTRAIT -> Surface.ROTATION_180
                else -> null
            }
        }
    }

    private fun Any?.isPcModeController(): Boolean {
        val context = this?.fieldOrNull("mContext") as? Context ?: return false
        return context.isPcOrDpMode()
    }

    private fun Context.isPcOrDpMode(): Boolean = contentResolver.isPcOrDpMode()

    private fun ContentResolver.isPcOrDpMode(): Boolean =
        runCatching {
            Settings.System.getInt(this, KEY_PC_MODE, 0) == 1 ||
                Settings.System.getInt(this, KEY_DP_PC_MODE, 0) == 1
        }.getOrDefault(false)

    private fun Context.isDpPcMode(): Boolean =
        runCatching {
            Settings.System.getInt(contentResolver, KEY_DP_PC_MODE, 0) == 1
        }.getOrDefault(false)

    @Suppress("DEPRECATION")
    private fun Context.isNaturalLandscape(): Boolean {
        return runCatching {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getRealSize(size)
            val currentLandscape = size.x >= size.y
            when (display.rotation) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> currentLandscape
                Surface.ROTATION_90, Surface.ROTATION_270 -> !currentLandscape
                else -> currentLandscape
            }
        }.getOrDefault(true)
    }

    private fun Any?.contextFromPcModeManager(): Context? =
        this?.fieldOrNull("mContext") as? Context

    private fun Any?.contextFromDisplayRotation(): Context? =
        this?.fieldOrNull("mContext") as? Context

    private fun Any?.shouldForceFrameworkDisplayRotation(context: Context): Boolean {
        if (!context.isPcOrDpMode()) return false
        val displayId = displayIdFromDisplayRotation()
        return !context.isDpPcMode() || displayId == null || displayId != Display.DEFAULT_DISPLAY
    }

    private fun Any?.displayIdFromDisplayRotation(): Int? {
        val displayContent = this?.fieldOrNull("mDisplayContent")
        return runCatching {
            XposedHelpers.callMethod(displayContent, "getDisplayId") as? Int
        }.getOrNull()
    }

    private fun Any?.displayIdsFromPcModeManager(): Set<Int> {
        val sparseArray = this?.fieldOrNull("mNavFmInfos") ?: return emptySet()
        return runCatching {
            val size = XposedHelpers.callMethod(sparseArray, "size") as? Int ?: 0
            val ids = (0 until size).mapNotNull { index ->
                XposedHelpers.callMethod(sparseArray, "keyAt", index) as? Int
            }
            ids.preferNonDefaultDisplays()
        }.getOrDefault(emptySet())
    }

    private fun Context.pcModeCandidateDisplayIds(): Set<Int> {
        val displays = runCatching {
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            displayManager?.displays.orEmpty().map { it.displayId }
        }.getOrDefault(emptyList())
        val contextDisplayId = runCatching { display.displayId }.getOrNull()
        return (displays + listOfNotNull(contextDisplayId)).distinct().preferNonDefaultDisplays()
    }

    private fun List<Int>.preferNonDefaultDisplays(): Set<Int> {
        val valid = filter { it != Display.INVALID_DISPLAY }.distinct()
        val nonDefault = valid.filter { it != Display.DEFAULT_DISPLAY }
        return (nonDefault.ifEmpty { valid }).toSet()
    }

    private fun windowManagerServiceOrNull(): Any? =
        runCatching {
            val windowManagerGlobal = Class.forName("android.view.WindowManagerGlobal")
            windowManagerGlobal.getDeclaredMethod("getWindowManagerService").invoke(null)
        }.onFailure { Log.w(TAG, "Get WindowManagerService failed", it) }.getOrNull()

    private fun Any.fieldOrNull(name: String): Any? =
        runCatching { XposedHelpers.getObjectField(this, name) }.getOrNull()

    private fun findFirstClass(classLoader: ClassLoader?, vararg names: String): Class<*>? =
        names.firstNotNullOfOrNull { XposedHelpers.findClassIfExists(it, classLoader) }

    private fun isCalledFromDesktopOrientationHandler(): Boolean =
        Thread.currentThread().stackTrace.any {
            it.methodName == "handleActivityOrientationChange" &&
                it.className.endsWith("DesktopActivityOrientationChangeHandler")
        }
}
