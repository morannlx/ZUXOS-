package me.inkdye.zuxos.hook.lenovo

import android.content.Context
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import me.inkdye.zuxos.hook.hookFirstMethod

object SafeCenterScanBlockerHook : Hook {
    private val packages = setOf("com.lenovo.safecenter", "com.zui.safecenter")

    override val prefKey = ModuleConfig.SAFE_CENTER_BLOCK_SCAN
    override val targetPackage = "com.zui.safecenter"
    override val targetPackages: Set<String> = packages
    override val moduleName = "safe_center"

    override fun PackageParam.onHook() {
        packages.forEach { target ->
            loadApp(target) {
                hookFirstMethod(
                    "com.lenovo.safecenter.antivirus.autoscan.AutoOverallScan",
                    "LocalOverallScanVirus",
                    Context::class.java,
                ) {
                    intercept(null)
                }
            }
        }
    }
}
