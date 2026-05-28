package me.inkdye.zuxos.corepatch

import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.config.ModuleConfig

open class CorePatchForS(prefs: XSharedPreferences) : CorePatchForR(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            "com.android.server.pm.PackageManagerService".hook {
                injectMember {
                    method {
                        name = "doesSignatureMatchForPermissions"
                        param(String::class.java, "com.android.server.pm.parsing.pkg.ParsedPackage".toClass(), Int::class.java)
                    }
                    afterHook {
                        if (prefs.getBoolean(ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && prefs.getBoolean(ModuleConfig.CORE_PATCH_USE_PRE_SIG, false)) {
                            if (result == false) {
                                val pPname = args[1]?.let {
                                    it::class.java.getMethod("getPackageName").invoke(it) as? String
                                } ?: return@afterHook
                                if (pPname == args[0] as? String) result = true
                            }
                        }
                    }
                }
            }
        }
    }
}
