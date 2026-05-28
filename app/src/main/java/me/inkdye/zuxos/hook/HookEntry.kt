package me.inkdye.zuxos.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.settings.AppInfoExtend

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    init {
        Hooks.register(
            AppInfoExtend,
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
            if (ModuleConfig.isEnabled(hook.prefKey, hook.defaultEnabled)) {
                with(hook) { onHook() }
            }
        }
    }
}
