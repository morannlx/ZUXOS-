package me.inkdye.zuxos.hook.systemframework

import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import me.inkdye.zuxos.hook.hookFirstMethodAny

object DisableFlagSecure : Hook {
    override val prefKey = ModuleConfig.DISABLE_FLAG_SECURE
    override val targetPackage = "android"
    override val moduleName = "system_framework"

    override fun PackageParam.onHook() {
        hookFirstMethodAny("com.android.server.wm.WindowState", "isSecureLocked") {
            intercept(false)
        }
    }
}
