package me.inkdye.zuxos.corepatch

import android.content.pm.Signature
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import java.io.RandomAccessFile
import java.util.Arrays

open class CorePatchForT(prefs: XSharedPreferences) : CorePatchForS(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)

            // ── checkDowngrade (T+: PackageManagerServiceUtils) ──
            val androidPackageCls = "com.android.server.pm.parsing.pkg.AndroidPackage".toClassOrNull()
            val pkgInfoLiteCls = "android.content.pm.PackageInfoLite".toClassOrNull()
            if (androidPackageCls != null && pkgInfoLiteCls != null) {
                "com.android.server.pm.PackageManagerServiceUtils".hook {
                    injectMember {
                        method {
                            name = "checkDowngrade"
                            param(androidPackageCls, pkgInfoLiteCls)
                        }
                        beforeHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = null }
                    }
                }
            }

            // ── checkCapability (T+: only digestCreak, no authcreak) ──
            val signingDetailsCls = getSigningDetailsClass()
            if (signingDetailsCls != null) {
                signingDetailsCls.hook {
                    injectMember {
                        method { name = "checkCapability" }
                        beforeHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)) {
                                val cap = args.getOrNull(1) as? Int
                                if (cap != null && cap != 4 && cap != 16) result = true
                            }
                        }
                    }
                }
            }

            // ── doesSignatureMatchForPermissions (moved to InstallPackageHelper in T) ──
            val parsedPackageCls = getParsedPackageClass()
            if (parsedPackageCls != null) {
                "com.android.server.pm.InstallPackageHelper".hook {
                    injectMember {
                        method {
                            name = "doesSignatureMatchForPermissions"
                            param(String::class.java, parsedPackageCls, Int::class.java)
                        }
                        afterHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_USE_PRE_SIG, false)) {
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

            // ── assertMinSignatureSchemeIsValid ──
            "com.android.server.pm.ScanPackageUtils".hook {
                injectMember {
                    method {
                        name = "assertMinSignatureSchemeIsValid"
                        param(parsedPackageCls ?: return@hook, Int::class.java)
                    }
                    afterHook { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = null }
                }
            }

            // ── StrictJarVerifier constructor: disable rollback protections ──
            "android.util.jar.StrictJarVerifier".toClassOrNull()?.let { cls ->
                cls.hook {
                    injectMember {
                        allConstructors()
                        afterHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) {
                                de.robv.android.xposed.XposedHelpers.setBooleanField(
                                    instance, "signatureSchemeRollbackProtectionsEnforced", false)
                            }
                        }
                    }
                }
            }

            // ── deoptimize canJoinSharedUserId ──
            "com.android.server.pm.PackageManagerServiceUtils".toClassOrNull()?.let { pmsUtilsCls ->
                runCatching { deoptimizeMethod(pmsUtilsCls, "canJoinSharedUserId") }
                    .onFailure { YLog.debug("$TAG: deopt canJoinSharedUserId skipped: ${it.message}") }
            }

            // ── ApkSigningBlockUtils: parseVerityDigest and verifyIntegrity ──
            val apkSigningBlockCls = "android.util.apk.ApkSigningBlockUtils".toClassOrNull()
            val signatureInfoCls = "android.util.apk.SignatureInfo".toClassOrNull()
            if (apkSigningBlockCls != null && signatureInfoCls != null) {
                apkSigningBlockCls.hook {
                    injectMember {
                        method {
                            name = "parseVerityDigestAndVerifySourceLength"
                            param(ByteArray::class.java, Long::class.javaPrimitiveType!!, signatureInfoCls)
                        }
                        beforeHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) {
                                result = Arrays.copyOfRange(args[0] as ByteArray, 0, 32)
                            }
                        }
                    }
                }
                apkSigningBlockCls.hook {
                    injectMember {
                        method {
                            name = "verifyIntegrityForVerityBasedAlgorithm"
                            param(ByteArray::class.java, RandomAccessFile::class.java, signatureInfoCls)
                        }
                        beforeHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = null
                        }
                    }
                }
            }
        }
    }

    fun getParsedPackageClass(): Class<*>? {
        return "com.android.server.pm.parsing.pkg.ParsedPackage".toClassOrNull()
            ?: "com.android.internal.pm.parsing.pkg.ParsedPackage".toClassOrNull()
    }

    fun getSigningDetailsClass(): Class<*>? {
        return "android.content.pm.SigningDetails".toClassOrNull()
            ?: "android.content.pm.PackageParser.SigningDetails".toClassOrNull()
    }
}
