package me.inkdye.zuxos.hook

import android.os.Build
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.corepatch.CorePatchForQ
import me.inkdye.zuxos.corepatch.CorePatchForR
import me.inkdye.zuxos.corepatch.CorePatchForS
import me.inkdye.zuxos.corepatch.CorePatchForT
import me.inkdye.zuxos.corepatch.CorePatchForU
import me.inkdye.zuxos.corepatch.CorePatchForV
import me.inkdye.zuxos.hook.settings.AppInfoExtend
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerAllowPermissionHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerDisableDeleteHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerDisableInstallerAdHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerDisableScanHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerSkipWarnPageHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerStyleHook
import me.inkdye.zuxos.hook.systemframework.DisableFlagSecure
import me.inkdye.zuxos.logging.HookLogLevel
import me.inkdye.zuxos.logging.HookLogStage
import de.robv.android.xposed.XSharedPreferences

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    init {
        Hooks.register(
            AppInfoExtend,
            PackageInstallerDisableScanHook,
            PackageInstallerAllowPermissionHook,
            PackageInstallerSkipWarnPageHook,
            PackageInstallerDisableInstallerAdHook,
            PackageInstallerStyleHook,
            PackageInstallerDisableDeleteHook,
            DisableFlagSecure,
        )
    }

    override fun onInit() = configs {
        isDebug = true
    }

    override fun onHook() = encase {
        loadApp(BuildConfig.APPLICATION_ID) {
            "me.inkdye.zuxos.ui.activity.LiquidGlassHomeActivity".toClass().method {
                name = "isModuleActivated"
            }.hook {
                replaceAny { true }
            }
        }

        Hooks.forEach { hook ->
            if (!hook.matchesPackage(packageName)) return@forEach
            HookLogger.installContextBridge(this)

            HookLogger.log(
                param = this,
                hook = hook,
                stage = HookLogStage.REGISTER,
                message = "Hook registered: key=${hook.prefKey}, target=${hook.targetPackage ?: packageName}",
            )

            val enabled = ModuleConfig.isEnabled(hook.prefKey, hook.defaultEnabled)
            HookLogger.log(
                param = this,
                hook = hook,
                stage = if (enabled) HookLogStage.ENABLED else HookLogStage.DISABLED,
                message = "Hook ${if (enabled) "enabled" else "disabled"} by preference ${hook.prefKey}",
                level = if (enabled) HookLogLevel.INFO else HookLogLevel.WARN,
            )
            if (!enabled) return@forEach

            HookLogger.log(
                param = this,
                hook = hook,
                stage = HookLogStage.EXECUTE_START,
                message = "Begin installing hook in package=$packageName process=$processName",
            )
            runCatching {
                with(hook) { onHook() }
            }.onSuccess {
                HookLogger.log(
                    param = this,
                    hook = hook,
                    stage = HookLogStage.EXECUTE_SUCCESS,
                    message = "Hook installed successfully",
                )
            }.onFailure { error ->
                HookLogger.log(
                    param = this,
                    hook = hook,
                    stage = HookLogStage.EXECUTE_FAILURE,
                    message = error.localizedMessage ?: error.javaClass.name,
                    level = HookLogLevel.ERROR,
                    throwable = error,
                )
            }
        }

        if (packageName == "android") {
            HookLogger.installContextBridge(this)
            val enabled = ModuleConfig.isEnabled(ModuleConfig.CORE_PATCH_ENABLED, false)
            HookLogger.log(
                param = this,
                hook = CorePatchHook,
                stage = if (enabled) HookLogStage.ENABLED else HookLogStage.DISABLED,
                message = "Core Patch ${if (enabled) "enabled" else "disabled"} by preference ${ModuleConfig.CORE_PATCH_ENABLED}",
                level = if (enabled) HookLogLevel.INFO else HookLogLevel.WARN,
            )
            if (enabled) installCorePatch()
        }
    }

    private fun Hook.matchesPackage(packageName: String): Boolean {
        val targets = targetPackages
        return targets.isEmpty() || packageName in targets
    }

    private fun PackageParam.installCorePatch() {
        HookLogger.log(
            param = this,
            hook = CorePatchHook,
            stage = HookLogStage.EXECUTE_START,
            message = "Begin installing Core Patch in package=$packageName process=$processName sdk=${Build.VERSION.SDK_INT}",
        )
        runCatching {
            val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)
            val handler = when {
                Build.VERSION.SDK_INT >= 35 -> CorePatchForV(prefs)
                Build.VERSION.SDK_INT == 34 -> CorePatchForU(prefs)
                Build.VERSION.SDK_INT == 33 -> CorePatchForT(prefs)
                Build.VERSION.SDK_INT in 31..32 -> CorePatchForS(prefs)
                Build.VERSION.SDK_INT == 30 -> CorePatchForR(prefs)
                Build.VERSION.SDK_INT in 28..29 -> CorePatchForQ(prefs)
                else -> CorePatchForV(prefs)
            }
            handler.onHook(this)
        }.onSuccess {
            HookLogger.log(
                param = this,
                hook = CorePatchHook,
                stage = HookLogStage.EXECUTE_SUCCESS,
                message = "Core Patch installed successfully",
            )
        }.onFailure { error ->
            HookLogger.log(
                param = this,
                hook = CorePatchHook,
                stage = HookLogStage.EXECUTE_FAILURE,
                message = error.localizedMessage ?: error.javaClass.name,
                level = HookLogLevel.ERROR,
                throwable = error,
            )
        }
    }

    private object CorePatchHook : Hook {
        override val prefKey = ModuleConfig.CORE_PATCH_ENABLED
        override val targetPackage = "android"
        override val moduleName = "core_patch"
        override fun PackageParam.onHook() = Unit
    }
}
