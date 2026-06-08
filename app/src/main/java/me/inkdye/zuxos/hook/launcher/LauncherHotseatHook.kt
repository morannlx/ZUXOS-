package me.inkdye.zuxos.hook.launcher

import android.content.Context
import android.content.Intent
import android.view.View
import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook

object LauncherHotseatHook : Hook {
    override val prefKey = ModuleConfig.LAUNCHER_HOTSEAT_EXPAND
    override val targetPackage = LauncherConstants.PACKAGE_ZUX_LAUNCHER
    override val moduleName = "zux_launcher_hotseat"

    private const val TAG = "ZUXOS.LauncherHotseat"

    override fun PackageParam.onHook() {
        val classLoader = appClassLoader
        hookHotseatMaxCount(classLoader)
        hookSpaceChecks(classLoader)
        hookDeviceProfile(classLoader)
        hookAddItemMethods(classLoader)
        hookDatabaseHotseatLimit(classLoader)
        hookLoaderCursor(classLoader)
        hookDatabaseOperations(classLoader)
        hookLauncherAppState(classLoader)
        hookCellLayoutMethods(classLoader)
    }

    private fun hookHotseatMaxCount(classLoader: ClassLoader?) {
        runHook("Hotseat.getMaxCount") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.Hotseat",
                classLoader,
                "getMaxCount",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = LauncherConstants.HOTSEAT_MAX_COUNT
                    }
                },
            )
        }
    }

    private fun hookDatabaseHotseatLimit(classLoader: ClassLoader?) {
        runHook("InvariantDeviceProfile.getNumDatabaseHotseatIcons") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.InvariantDeviceProfile",
                classLoader,
                "getNumDatabaseHotseatIcons",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = LauncherConstants.HOTSEAT_MAX_COUNT
                    }
                },
            )
        }
        runHook("InvariantDeviceProfile.numDatabaseHotseatIcons") {
            val clazz = XposedHelpers.findClassIfExists("com.android.launcher3.InvariantDeviceProfile", classLoader)
                ?: return@runHook
            XposedHelpers.setStaticIntField(clazz, "numDatabaseHotseatIcons", LauncherConstants.HOTSEAT_MAX_COUNT)
        }
    }

    private fun hookSpaceChecks(classLoader: ClassLoader?) {
        runHook("Launcher.showOutOfSpaceMessage") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.Launcher",
                classLoader,
                "showOutOfSpaceMessage",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = null
                    }
                },
            )
        }
        runHook("Launcher.checkOccupiedShortcut") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.Launcher",
                classLoader,
                "checkOccupiedShortcut",
                View::class.java,
                "com.android.launcher3.model.data.WorkspaceItemInfo",
                "com.android.launcher3.Workspace",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                },
            )
        }
    }

    private fun hookDeviceProfile(classLoader: ClassLoader?) {
        runHook("DeviceProfile.getHotseatColumnSpan") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.DeviceProfile",
                classLoader,
                "getHotseatColumnSpan",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = LauncherConstants.HOTSEAT_MAX_COUNT
                    }
                },
            )
        }
        runHook("DeviceProfile.recalculateHotseatWidthAndBorderSpace") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.DeviceProfile",
                classLoader,
                "recalculateHotseatWidthAndBorderSpace",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.setIntField("numShownHotseatIcons", LauncherConstants.HOTSEAT_MAX_COUNT)
                    }
                },
            )
        }
    }

    private fun hookAddItemMethods(classLoader: ClassLoader?) {
        runHook("Launcher.completeAddShortcut") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.Launcher",
                classLoader,
                "completeAddShortcut",
                Intent::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                "com.android.launcher3.util.PendingRequestArgs",
                object : XC_MethodHook() {},
            )
        }
        runHook("Launcher.addPendingItem") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.Launcher",
                classLoader,
                "addPendingItem",
                "com.android.launcher3.PendingAddItemInfo",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                IntArray::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {},
            )
        }
        runHook("Launcher.addToWorkspace") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.Launcher",
                classLoader,
                "addToWorkspace",
                "com.android.launcher3.model.data.ItemInfo",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {},
            )
        }
    }

    private fun hookLoaderCursor(classLoader: ClassLoader?) {
        val itemInfoClassName = "com.android.launcher3.model.data.ItemInfo"
        val hotseatPlacementHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val itemInfo = param.args.firstOrNull() ?: return
                val container = itemInfo.intFieldOrNull("container") ?: return
                val screenId = itemInfo.intFieldOrNull("screenId") ?: return
                if (container == LauncherConstants.HOTSEAT_CONTAINER &&
                    screenId in 0 until LauncherConstants.HOTSEAT_MAX_COUNT
                ) {
                    param.result = true
                }
            }
        }
        runHook("LoaderCursor.checkItemPlacement(ItemInfo)") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.model.LoaderCursor",
                classLoader,
                "checkItemPlacement",
                itemInfoClassName,
                hotseatPlacementHook,
            )
        }
        runHook("LoaderCursor.checkItemPlacement(ItemInfo, boolean)") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.model.LoaderCursor",
                classLoader,
                "checkItemPlacement",
                itemInfoClassName,
                Boolean::class.javaPrimitiveType,
                hotseatPlacementHook,
            )
        }
        runHook("LoaderCursor.b") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.model.LoaderCursor",
                classLoader,
                "b",
                itemInfoClassName,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val itemInfo = param.args.firstOrNull() ?: return
                        if (itemInfo.intFieldOrNull("container") == LauncherConstants.HOTSEAT_CONTAINER) {
                            param.result = false
                        }
                    }
                },
            )
        }
        runHook("LoaderCursor.checkAndAddItem") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.model.LoaderCursor",
                classLoader,
                "checkAndAddItem",
                itemInfoClassName,
                "com.android.launcher3.model.BgDataModel",
                object : XC_MethodHook() {},
            )
        }
    }

    private fun hookDatabaseOperations(classLoader: ClassLoader?) {
        runHook("LauncherModel.addOrMoveItemInDatabase") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.LauncherModel",
                classLoader,
                "addOrMoveItemInDatabase",
                "com.android.launcher3.model.data.ItemInfo",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val container = param.args.getOrNull(1) as? Int ?: return
                        val screen = param.args.getOrNull(2) as? Int ?: return
                        if (container == LauncherConstants.HOTSEAT_CONTAINER &&
                            screen in 0 until LauncherConstants.HOTSEAT_MAX_COUNT
                        ) {
                            val itemInfo = param.args.firstOrNull()
                            itemInfo?.setIntField("container", LauncherConstants.HOTSEAT_CONTAINER)
                        }
                    }
                },
            )
        }
    }

    private fun hookLauncherAppState(classLoader: ClassLoader?) {
        runHook("LauncherAppState.getInstance") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.LauncherAppState",
                classLoader,
                "getInstance",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val appState = param.result ?: return
                        val invariantProfile = appState.objectFieldOrNull("mInvariantDeviceProfile") ?: return
                        invariantProfile.setIntField("numDatabaseHotseatIcons", LauncherConstants.HOTSEAT_MAX_COUNT)
                    }
                },
            )
        }
    }

    private fun hookCellLayoutMethods(classLoader: ClassLoader?) {
        runHook("CellLayout.findCellForSpan") {
            XposedHelpers.findAndHookMethod(
                "com.android.launcher3.CellLayout",
                classLoader,
                "findCellForSpan",
                IntArray::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (param.result as? Boolean == true) return
                        val cellXY = param.args.firstOrNull() as? IntArray ?: return
                        cellXY[0] = 0
                        cellXY[1] = 0
                        param.result = true
                    }
                },
            )
        }
    }

    private fun runHook(name: String, block: () -> Unit) {
        runCatching(block).onFailure { Log.w(TAG, "Hook failed: $name", it) }
    }

    private fun Any.intFieldOrNull(name: String): Int? =
        runCatching { XposedHelpers.getIntField(this, name) }.getOrNull()

    private fun Any.objectFieldOrNull(name: String): Any? =
        runCatching { XposedHelpers.getObjectField(this, name) }.getOrNull()

    private fun Any.setIntField(name: String, value: Int) {
        runCatching { XposedHelpers.setIntField(this, name, value) }
    }
}
