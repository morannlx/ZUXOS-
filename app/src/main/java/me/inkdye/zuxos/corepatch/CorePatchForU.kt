package me.inkdye.zuxos.corepatch

import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.findHostClass
import me.inkdye.zuxos.hook.hookFirstMethod
import java.lang.reflect.Modifier

open class CorePatchForU(prefs: XSharedPreferences) : CorePatchForT(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)

            // ── ReconcilePackageUtils deoptimize ──
            findHostClass("com.android.server.pm.ReconcilePackageUtils")?.let {
                runCatching { deoptimizeMethod(it, "reconcilePackages") }
                    .onFailure { logError("deopt reconcilePackages failed", it) }
            }

            // ── ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS field hack ──
            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_SHARED_USER, false)) {
                runCatching {
                    val rc = findHostClass("com.android.server.pm.ReconcilePackageUtils") ?: return@runCatching
                    val f = rc.getDeclaredField("ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS")
                    f.isAccessible = true
                    val af = java.lang.reflect.Field::class.java.getDeclaredField("accessFlags")
                    af.isAccessible = true; af.setInt(f, af.getInt(f) and Modifier.FINAL.inv())
                    f.set(null, true)
                }.onFailure { logError("ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS failed", it) }
            }

            // ── checkDowngrade (new package paths) ──
            val androidPackageCls = findHostClass("com.android.server.pm.pkg.AndroidPackage")
            val pkgInfoLiteCls = findHostClass("android.content.pm.PackageInfoLite")
            if (androidPackageCls != null && pkgInfoLiteCls != null) {
                hookFirstMethod(
                    "com.android.server.pm.PackageManagerServiceUtils",
                    "checkDowngrade",
                    androidPackageCls,
                    pkgInfoLiteCls,
                ) {
                    before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = null }
                }
            }

            // ── assertMinSignatureSchemeIsValid (new paths) ──
            if (androidPackageCls != null) {
                hookFirstMethod(
                    "com.android.server.pm.ScanPackageUtils",
                    "assertMinSignatureSchemeIsValid",
                    androidPackageCls,
                    Int::class.java,
                ) {
                    after { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = null }
                }
            }

            // ── Nothing Phone block bypass ──
            val ntConfigCls = findHostClass("com.nothing.server.ex.NtConfigListServiceImpl")
            hookFirstMethod(ntConfigCls, "isInstallingAppForbidden", String::class.java) {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_BYPASS_BLOCK, true)) result = false }
            }
            hookFirstMethod(ntConfigCls, "isStartingAppForbidden", String::class.java) {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_BYPASS_BLOCK, true)) result = false }
            }
        }
    }
}
