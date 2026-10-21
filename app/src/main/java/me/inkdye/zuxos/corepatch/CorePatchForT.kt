package me.inkdye.zuxos.corepatch

import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.findHostClass
import me.inkdye.zuxos.hook.hookAllConstructors
import me.inkdye.zuxos.hook.hookFirstMethod
import me.inkdye.zuxos.hook.hookFirstMethodAny
import java.io.RandomAccessFile
import java.util.Arrays

open class CorePatchForT(prefs: XSharedPreferences) : CorePatchForS(prefs) {
    override fun onHook(param: PackageParam) {
        super.onHook(param)
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)

            // ── checkDowngrade (T+: PackageManagerServiceUtils) ──
            val androidPackageCls = findHostClass("com.android.server.pm.parsing.pkg.AndroidPackage")
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

            // ── checkCapability (T+: only digestCreak, no authcreak) ──
            val signingDetailsCls = getSigningDetailsClass()
            if (signingDetailsCls != null) {
                hookFirstMethodAny(signingDetailsCls, "checkCapability") {
                    before {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)) {
                            val cap = args.getOrNull(1) as? Int
                            if (cap != null && cap != 4 && cap != 16) result = true
                        }
                    }
                }
            }

            // ── doesSignatureMatchForPermissions (moved to InstallPackageHelper in T) ──
            val parsedPackageCls = getParsedPackageClass()
            if (parsedPackageCls != null) {
                hookFirstMethod(
                    "com.android.server.pm.InstallPackageHelper",
                    "doesSignatureMatchForPermissions",
                    String::class.java,
                    parsedPackageCls,
                    Int::class.java,
                ) {
                    after {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_USE_PRE_SIG, false)) {
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

            // ── assertMinSignatureSchemeIsValid ──
            if (parsedPackageCls != null) {
                hookFirstMethod(
                    "com.android.server.pm.ScanPackageUtils",
                    "assertMinSignatureSchemeIsValid",
                    parsedPackageCls,
                    Int::class.java,
                ) {
                    after { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = null }
                }
            }

            // ── StrictJarVerifier constructor: disable rollback protections ──
            hookAllConstructors(findHostClass("android.util.jar.StrictJarVerifier")) {
                after {
                    if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) {
                        de.robv.android.xposed.XposedHelpers.setBooleanField(
                            instance, "signatureSchemeRollbackProtectionsEnforced", false)
                    }
                }
            }

            // ── deoptimize canJoinSharedUserId ──
            findHostClass("com.android.server.pm.PackageManagerServiceUtils")?.let { pmsUtilsCls ->
                runCatching { deoptimizeMethod(pmsUtilsCls, "canJoinSharedUserId") }
                    .onFailure { YLog.debug("$TAG: deopt canJoinSharedUserId skipped: ${it.message}") }
            }

            // ── ApkSigningBlockUtils: parseVerityDigest and verifyIntegrity ──
            val apkSigningBlockCls = findHostClass("android.util.apk.ApkSigningBlockUtils")
            val signatureInfoCls = findHostClass("android.util.apk.SignatureInfo")
            if (apkSigningBlockCls != null && signatureInfoCls != null) {
                hookFirstMethod(
                    apkSigningBlockCls,
                    "parseVerityDigestAndVerifySourceLength",
                    ByteArray::class.java,
                    Long::class.javaPrimitiveType!!,
                    signatureInfoCls,
                ) {
                    before {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) {
                            result = Arrays.copyOfRange(args[0] as ByteArray, 0, 32)
                        }
                    }
                }
                hookFirstMethod(
                    apkSigningBlockCls,
                    "verifyIntegrityForVerityBasedAlgorithm",
                    ByteArray::class.java,
                    RandomAccessFile::class.java,
                    signatureInfoCls,
                ) {
                    before {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = null
                    }
                }
            }
        }
    }

    private fun PackageParam.getParsedPackageClass(): Class<*>? {
        return findHostClass("com.android.server.pm.parsing.pkg.ParsedPackage")
            ?: findHostClass("com.android.internal.pm.parsing.pkg.ParsedPackage")
    }

    private fun PackageParam.getSigningDetailsClass(): Class<*>? {
        return findHostClass("android.content.pm.SigningDetails")
            ?: findHostClass("android.content.pm.PackageParser.SigningDetails")
    }
}
