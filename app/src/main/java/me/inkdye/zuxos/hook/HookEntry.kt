package me.inkdye.zuxos.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import me.inkdye.zuxos.BuildConfig

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    init {
        // 在此注册 Hook 模块
        // Hooks.register(
        //     ExampleHook,
        // )
    }

    override fun onInit() = configs {
        isDebug = true
    }

    override fun onHook() = encase {
        loadApp(BuildConfig.APPLICATION_ID) {
            "me.inkdye.zuxos.ui.activity.MainActivity".toClass().method {
                name = "isModuleActivated"
            }.hook {
                replaceAny { true }
            }
        }

        Hooks.forEach { hook ->
            if (prefs.getBoolean(hook.prefKey, hook.defaultEnabled)) {
                with(hook) { onHook() }
            }
        }
    }
}
