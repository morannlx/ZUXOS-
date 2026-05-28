package me.inkdye.zuxos.corepatch

import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import java.lang.reflect.Modifier

open class CorePatchForU(prefs: XSharedPreferences) : CorePatchForT(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)

            // ── ReconcilePackageUtils deoptimize ──
            "com.android.server.pm.ReconcilePackageUtils".toClassOrNull()?.let {
                runCatching { deoptimizeMethod(it, "reconcilePackages") }
                    .onFailure { logError("deopt reconcilePackages failed", it) }
            }

            // ── ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS field hack ──
            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_SHARED_USER, false)) {
                runCatching {
                    val rc = "com.android.server.pm.ReconcilePackageUtils".toClassOrNull() ?: return@runCatching
                    val f = rc.getDeclaredField("ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS")
                    f.isAccessible = true
                    val af = java.lang.reflect.Field::class.java.getDeclaredField("accessFlags")
                    af.isAccessible = true; af.setInt(f, af.getInt(f) and Modifier.FINAL.inv())
                    f.set(null, true)
                }.onFailure { logError("ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS failed", it) }
            }

            // ── checkDowngrade (new package paths) ──
            val androidPackageCls = "com.android.server.pm.pkg.AndroidPackage".toClassOrNull()
            val pkgInfoLiteCls = "android.content.pm.PackageInfoLite".toClassOrNull()
            if (androidPackageCls != null && pkgInfoLiteCls != null) {
                "com.android.server.pm.PackageManagerServiceUtils".hook {
                    injectMember {
                        method { name = "checkDowngrade"; param(androidPackageCls, pkgInfoLiteCls) }
                        beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = null }
                    }
                }
            }

            // ── assertMinSignatureSchemeIsValid (new paths) ──
            if (androidPackageCls != null) {
                "com.android.server.pm.ScanPackageUtils".hook {
                    injectMember {
                        method { name = "assertMinSignatureSchemeIsValid"; param(androidPackageCls, Int::class.java) }
                        afterHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = null }
                    }
                }
            }

            // ── Nothing Phone block bypass ──
            "com.nothing.server.ex.NtConfigListServiceImpl".toClassOrNull()?.let { nt ->
                nt.hook { injectMember { method { name = "isInstallingAppForbidden"; param(String::class.java) }
                    beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_BYPASS_BLOCK, true)) result = false } } }
                nt.hook { injectMember { method { name = "isStartingAppForbidden"; param(String::class.java) }
                    beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_BYPASS_BLOCK, true)) result = false } } }
            }
        }
    }
}
