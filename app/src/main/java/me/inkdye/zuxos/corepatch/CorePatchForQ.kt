package me.inkdye.zuxos.corepatch

import android.content.pm.ApplicationInfo
import android.content.pm.Signature
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.findHostClass
import me.inkdye.zuxos.hook.hookFirstMethodAny
import java.lang.reflect.Field

open class CorePatchForQ(prefs: XSharedPreferences) : CorePatchBase(prefs) {
    override fun onHook(param: PackageParam) {
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)
            val signingDetailsCls = findHostClass("android.content.pm.PackageParser.SigningDetails")
            val packageCl = findHostClass("android.content.pm.PackageParser.Package")
            val packageManagerCls = findHostClass("com.android.server.pm.PackageManagerService")

            // ── checkDowngrade with version-code zeroing ──
            if (packageCl != null) {
                hookFirstMethodAny(packageManagerCls, "checkDowngrade") {
                    before {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) {
                            val pkg = args[0]
                            try { packageCl.getField("mVersionCode").apply { isAccessible = true }.set(pkg, 0) } catch (_: Exception) {}
                            try { packageCl.getField("mVersionCodeMajor").apply { isAccessible = true }.set(pkg, 0) } catch (_: Exception) {}
                        }
                    }
                }
            }

            // ── Digest verification bypass ──
            val strictJarVerifierCls = findHostClass("android.util.jar.StrictJarVerifier")
            for (method in listOf("verifyMessageDigest", "verify")) {
                hookFirstMethodAny(strictJarVerifierCls, method) {
                    before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
                }
            }
            hookFirstMethodAny(findHostClass("java.security.MessageDigest"), "isEqual") {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
            }
            hookFirstMethodAny(
                findHostClass("com.android.server.pm.PackageManagerServiceUtils"),
                "verifySignatures",
            ) {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = false }
            }

            // ── verifyV1Signature fallback ──
            val ppeCls = findHostClass("android.content.pm.PackageParser.PackageParserException")
            val errField: Field? = ppeCls?.getDeclaredField("error")?.apply { isAccessible = true }
            val sdCtor = signingDetailsCls?.getDeclaredConstructor(Signature::class.java, Integer.TYPE)?.apply { isAccessible = true }

            hookFirstMethodAny(findHostClass("android.util.apk.ApkSignatureVerifier"), "verifyV1Signature") {
                after {
                    if (!readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) return@after
                    val ex = throwable
                    if (ex != null) {
                        val cause = ex.cause
                        if (ex::class.java == ppeCls && errField?.getInt(ex) == -103) {
                            val sig = Signature(SIGNATURE)
                            result = sdCtor?.newInstance(arrayOf(sig), 1)
                        }
                        if (cause != null && cause::class.java == ppeCls && errField?.getInt(cause) == -103) {
                            val sig = Signature(SIGNATURE)
                            result = sdCtor?.newInstance(arrayOf(sig), 1)
                        }
                    }
                }
            }

            // ── checkCapability + checkCapabilityRecover (Q-specific) ──
            if (signingDetailsCls != null) {
                for (cap in listOf("checkCapability", "checkCapabilityRecover")) {
                    hookFirstMethodAny(signingDetailsCls, cap) {
                        before {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) {
                                val capVal = args.getOrNull(1) as? Int
                                if (capVal != null && capVal != 4 && capVal != 16) result = true
                            }
                        }
                    }
                }

                // ── signaturesMatchExactly ──
                hookFirstMethodAny(signingDetailsCls, "signaturesMatchExactly") {
                    before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_EXACT_SIG_CHECK, false)) result = true }
                }
            }

            // ── Hidden API whitelist for system apps ──
            hookFirstMethodAny(
                findHostClass("android.content.pm.ApplicationInfo"),
                "isPackageWhitelistedForHiddenApis",
            ) {
                before {
                    if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)) {
                        val info = instance as? ApplicationInfo
                        if (info != null && ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                || (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {
                            result = true
                        }
                    }
                }
            }

            // ── KeySet bypass ──
            val ksMgr = findHostClass("com.android.server.pm.KeySetManagerService")
            if (ksMgr != null) {
                val bypass = ThreadLocal<Boolean>()
                hookFirstMethodAny(ksMgr, "shouldCheckUpgradeKeySetLocked") {
                    after {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)
                            && Thread.currentThread().stackTrace.any {
                                it.methodName in listOf("preparePackageLI", "installPackageLI")
                            }) {
                            bypass.set(true); result = true
                        } else { bypass.set(false) }
                    }
                }
                hookFirstMethodAny(ksMgr, "checkUpgradeKeySetLocked") {
                    after {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && bypass.get() == true) result = true
                    }
                }
            }

            // ── Disable verification agent ──
            hookFirstMethodAny(packageManagerCls, "isVerificationEnabled") {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DISABLE_VERIFICATION_AGENT, false)) result = false }
            }
        }
    }
}
