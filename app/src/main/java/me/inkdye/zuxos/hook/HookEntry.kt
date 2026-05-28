package me.inkdye.zuxos.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.settings.AppInfoExtend
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerAllowPermissionHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerDisableDeleteHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerDisableInstallerAdHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerDisableScanHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerSkipWarnPageHook
import me.inkdye.zuxos.hook.packageinstaller.PackageInstallerStyleHook
import me.inkdye.zuxos.logging.HookLogLevel
import me.inkdye.zuxos.logging.HookLogStage

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
    }

    private fun Hook.matchesPackage(packageName: String): Boolean {
        val targets = targetPackages
        return targets.isEmpty() || packageName in targets
    }
}
