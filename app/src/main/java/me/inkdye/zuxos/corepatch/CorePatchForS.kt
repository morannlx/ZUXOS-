package me.inkdye.zuxos.corepatch

import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.findHostClass
import me.inkdye.zuxos.hook.hookFirstMethod

open class CorePatchForS(prefs: XSharedPreferences) : CorePatchForR(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            val parsedPackageCls = findHostClass("com.android.server.pm.parsing.pkg.ParsedPackage") ?: return@run
            hookFirstMethod(
                "com.android.server.pm.PackageManagerService",
                "doesSignatureMatchForPermissions",
                String::class.java,
                parsedPackageCls,
                Int::class.java,
            ) {
                after {
                    if (readConf(this@CorePatchForS.prefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(this@CorePatchForS.prefs, ModuleConfig.CORE_PATCH_USE_PRE_SIG, false)) {
                        if (result == false) {
                            val pPname = args[1]?.let {
                                it::class.java.getMethod("getPackageName").invoke(it) as? String
                            } ?: return@after
                            if (pPname == args[0] as? String) result = true
                        }
                    }
                }
            }
        }
    }
}
