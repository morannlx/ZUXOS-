package me.inkdye.zuxos.hook.launcher

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import java.util.ArrayList

object LauncherForceStopHook : Hook {
    override val prefKey = ModuleConfig.LAUNCHER_DISABLE_FORCE_STOP
    override val targetPackage = LauncherConstants.PACKAGE_ZUX_LAUNCHER
    override val moduleName = "zux_launcher_force_stop"

    private const val TAG = "ZUXOS.LauncherForceStop"

    override fun PackageParam.onHook() {
        val classLoader = hostClassLoader
        hookOverviewUtilities(classLoader)
        hookLegacyActivityManagerWrapper(classLoader)
    }

    private fun hookOverviewUtilities(classLoader: ClassLoader?) {
        runHook("OverviewUtilities.removeAppProcess") {
            XposedHelpers.findAndHookMethod(
                "com.zui.launcher.util.OverviewUtilities",
                classLoader,
                "removeAppProcess",
                Context::class.java,
                Int::class.javaPrimitiveType,
                String::class.java,
                Int::class.javaPrimitiveType,
                blockPackageKillHook(packageArgIndex = 2),
            )
        }
        runHook("OverviewUtilities.c") {
            XposedHelpers.findAndHookMethod(
                "com.zui.launcher.util.OverviewUtilities",
                classLoader,
                "c",
                Context::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                blockPackageKillHook(packageArgIndex = 1),
            )
        }
        runHook("OverviewUtilities.removeAllRunningAppProcesses") {
            XposedHelpers.findAndHookMethod(
                "com.zui.launcher.util.OverviewUtilities",
                classLoader,
                "removeAllRunningAppProcesses",
                Context::class.java,
                ArrayList::class.java,
                Boolean::class.javaPrimitiveType,
                blockBatchKillHook(tasksArgIndex = 1),
            )
        }
    }

    private fun hookLegacyActivityManagerWrapper(classLoader: ClassLoader?) {
        val wrapperClass = XposedHelpers.findClassIfExists(
            "com.android.systemui.shared.system.ActivityManagerWrapper",
            classLoader,
        ) ?: return
        runHook("ActivityManagerWrapper.removeAppProcess") {
            XposedHelpers.findAndHookMethod(
                wrapperClass,
                "removeAppProcess",
                Context::class.java,
                Int::class.javaPrimitiveType,
                String::class.java,
                Int::class.javaPrimitiveType,
                blockPackageKillHook(packageArgIndex = 2),
            )
        }
        runHook("ActivityManagerWrapper.removeAllRunningAppProcesses") {
            XposedHelpers.findAndHookMethod(
                wrapperClass,
                "removeAllRunningAppProcesses",
                Context::class.java,
                ArrayList::class.java,
                blockBatchKillHook(tasksArgIndex = 1),
            )
        }
    }

    private fun blockPackageKillHook(packageArgIndex: Int) = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val packageName = param.args.getOrNull(packageArgIndex) as? String ?: return
            if (isProtectedPackage(packageName)) {
                param.result = null
            }
        }
    }

    private fun blockBatchKillHook(tasksArgIndex: Int) = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val tasks = param.args.getOrNull(tasksArgIndex) as? Iterable<*> ?: return
            val hasProtectedTask = tasks.any { task ->
                task.packageNameFromTask()?.let { isProtectedPackage(it) } == true
            }
            if (hasProtectedTask) {
                param.result = null
            }
        }
    }

    private fun isProtectedPackage(packageName: String): Boolean {
        if (!ModuleConfig.isEnabled(ModuleConfig.LAUNCHER_FORCE_STOP_WHITELIST_ENABLE, false)) {
            return true
        }
        return packageName in readWhitelist()
    }

    private fun readWhitelist(): Set<String> =
        ModuleConfig.readString(ModuleConfig.LAUNCHER_FORCE_STOP_WHITELIST, "")
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

    private fun Any?.packageNameFromTask(): String? {
        if (this == null) return null
        return componentNamePackage() ?: stringField("packageName") ?: baseActivityInfoPackage() ?: taskDescriptionPackage()
    }

    private fun Any.componentNamePackage(): String? {
        val component = objectFieldOrNull("componentName")
        return when (component) {
            is ComponentName -> component.packageName
            else -> component?.callString("getPackageName")
        }
    }

    private fun Any.baseActivityInfoPackage(): String? =
        objectFieldOrNull("baseActivityInfo")?.stringField("packageName")

    private fun Any.taskDescriptionPackage(): String? =
        objectFieldOrNull("taskDescription")?.callString("getPackageName")

    private fun Any.objectFieldOrNull(name: String): Any? =
        runCatching { XposedHelpers.getObjectField(this, name) }.getOrNull()

    private fun Any.stringField(name: String): String? =
        objectFieldOrNull(name) as? String

    private fun Any.callString(methodName: String): String? =
        runCatching { XposedHelpers.callMethod(this, methodName) as? String }.getOrNull()

    private fun runHook(name: String, block: () -> Unit) {
        runCatching(block).onFailure { Log.w(TAG, "Hook failed: $name", it) }
    }
}
