package me.inkdye.zuxos.corepatch

import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.findHostClass
import me.inkdye.zuxos.hook.hookFirstMethod

open class CorePatchForV(prefs: XSharedPreferences) : CorePatchForU(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)
            val pmsUtils = findHostClass("com.android.server.pm.PackageManagerServiceUtils") ?: return@run
            val pkgSetting = findHostClass("com.android.server.pm.PackageSetting") ?: return@run
            val pkgInfoLite = findHostClass("android.content.pm.PackageInfoLite") ?: return@run
            hookFirstMethod(pmsUtils, "checkDowngrade", pkgSetting, pkgInfoLite) {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = null }
            }
        }
    }
}
