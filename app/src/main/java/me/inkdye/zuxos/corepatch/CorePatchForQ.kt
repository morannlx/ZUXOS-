package me.inkdye.zuxos.corepatch
import android.content.pm.ApplicationInfo
import android.content.pm.Signature
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

open class CorePatchForQ(prefs: XSharedPreferences) : CorePatchBase(prefs) {
    override fun onHook(param: PackageParam) {
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)
            val signingDetailsCls = "android.content.pm.PackageParser.SigningDetails".toClassOrNull()
            val packageCl = "android.content.pm.PackageParser.Package".toClassOrNull()

            // ── checkDowngrade with version-code zeroing ──
            if (packageCl != null) {
                "com.android.server.pm.PackageManagerService".hook {
                    injectMember {
                        method { name = "checkDowngrade" }
                        beforeHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) {
                                val pkg = args[0]
                                try { packageCl.getField("mVersionCode").apply { isAccessible = true }.set(pkg, 0) } catch (_: Exception) {}
                                try { packageCl.getField("mVersionCodeMajor").apply { isAccessible = true }.set(pkg, 0) } catch (_: Exception) {}
                            }
                        }
                    }
                }
            }

            // ── Digest verification bypass ──
            for (method in listOf("verifyMessageDigest", "verify")) {
                "android.util.jar.StrictJarVerifier".hook {
                    injectMember {
                        method { name = method }
                        beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
                    }
                }
            }
            "java.security.MessageDigest".hook {
                injectMember {
                    method { name = "isEqual" }
                    beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
                }
            }
            "com.android.server.pm.PackageManagerServiceUtils".hook {
                injectMember {
                    method { name = "verifySignatures" }
                    beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = false }
                }
            }

            // ── verifyV1Signature fallback ──
            val ppeCls = "android.content.pm.PackageParser.PackageParserException".toClassOrNull()
            val errField: Field? = ppeCls?.getDeclaredField("error")?.apply { isAccessible = true }
            val sdCtor = signingDetailsCls?.getDeclaredConstructor(Signature::class.java, Integer.TYPE)?.apply { isAccessible = true }

            "android.util.apk.ApkSignatureVerifier".hook {
                injectMember {
                    method { name = "verifyV1Signature" }
                    afterHook {
                        if (!readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) return@afterHook
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
            }

            // ── checkCapability + checkCapabilityRecover (Q-specific) ──
            if (signingDetailsCls != null) {
                for (cap in listOf("checkCapability", "checkCapabilityRecover")) {
                    signingDetailsCls.hook {
                        injectMember {
                            method { name = cap }
                            beforeHook {
                                if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) {
                                    val capVal = args.getOrNull(1) as? Int
                                    if (capVal != null && capVal != 4 && capVal != 16) result = true
                                }
                            }
                        }
                    }
                }

                // ── signaturesMatchExactly ──
                signingDetailsCls.hook {
                    injectMember {
                        method { name = "signaturesMatchExactly" }
                        beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_EXACT_SIG_CHECK, false)) result = true }
                    }
                }
            }

            // ── Hidden API whitelist for system apps ──
            "android.content.pm.ApplicationInfo".hook {
                injectMember {
                    method { name = "isPackageWhitelistedForHiddenApis" }
                    beforeHook {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)) {
                            val info = instance as? ApplicationInfo
                            if (info != null && ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                || (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {
                                result = true
                            }
                        }
                    }
                }
            }

            // ── KeySet bypass ──
            val ksMgr = "com.android.server.pm.KeySetManagerService".toClassOrNull()
            if (ksMgr != null) {
                val bypass = ThreadLocal<Boolean>()
                ksMgr.hook {
                    injectMember {
                        method { name = "shouldCheckUpgradeKeySetLocked" }
                        afterHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)
                                && Thread.currentThread().stackTrace.any {
                                    it.methodName in listOf("preparePackageLI", "installPackageLI")
                                }) {
                                bypass.set(true); result = true
                            } else { bypass.set(false) }
                        }
                    }
                }
                ksMgr.hook {
                    injectMember {
                        method { name = "checkUpgradeKeySetLocked" }
                        afterHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && bypass.get() == true) result = true
                        }
                    }
                }
            }

            // ── Disable verification agent ──
            "com.android.server.pm.PackageManagerService".hook {
                injectMember {
                    method { name = "isVerificationEnabled" }
                    beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DISABLE_VERIFICATION_AGENT, false)) result = false }
                }
            }
        }
    }
}
