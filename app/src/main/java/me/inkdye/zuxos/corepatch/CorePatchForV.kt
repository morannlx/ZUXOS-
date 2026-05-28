package me.inkdye.zuxos.corepatch

import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig

open class CorePatchForV(prefs: XSharedPreferences) : CorePatchForU(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)
            val pmsUtils = "com.android.server.pm.PackageManagerServiceUtils".toClassOrNull() ?: return@run
            val pkgSetting = "com.android.server.pm.PackageSetting".toClassOrNull() ?: return@run
            val pkgInfoLite = "android.content.pm.PackageInfoLite".toClassOrNull() ?: return@run
            pmsUtils.hook {
                injectMember {
                    method { name = "checkDowngrade"; param(pkgSetting, pkgInfoLite) }
                    beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = null }
                }
            }
        }
    }
}
