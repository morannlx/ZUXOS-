package me.inkdye.zuxos.hook.systemframework

import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook

object DisableFlagSecure : Hook {
    override val prefKey = ModuleConfig.DISABLE_FLAG_SECURE
    override val targetPackage = "android"
    override val moduleName = "system_framework"

    override fun PackageParam.onHook() {
        "com.android.server.wm.WindowState".hook {
            injectMember {
                method {
                    name = "isSecureLocked"
                }
                replaceAny { false }
            }
        }
    }
}
