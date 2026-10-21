package me.inkdye.zuxos.corepatch

import android.annotation.TargetApi
import android.os.Build
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.findHostClass
import me.inkdye.zuxos.hook.hookFirstMethod
import me.inkdye.zuxos.hook.hookFirstMethodAny
import me.inkdye.zuxos.hook.hookFirstMethodByCount
import java.lang.reflect.Member
import java.lang.reflect.Method

@TargetApi(Build.VERSION_CODES.R)
open class CorePatchForR(prefs: XSharedPreferences) : CorePatchBase(prefs) {

    private val deoptimizeMethod: Method?
    init {
        var m: Method? = null
        try {
            m = XposedBridge::class.java.getDeclaredMethod("deoptimizeMethod", Member::class.java)
        } catch (t: Throwable) { YLog.error("$TAG: cannot find deoptimizeMethod", t) }
        deoptimizeMethod = m
    }

    override fun onHook(param: PackageParam) {
        param.run {
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)
            val packageManagerCls = findHostClass("com.android.server.pm.PackageManagerService")
            val packageInfoLiteCls = findHostClass("android.content.pm.PackageInfoLite")

            // Downgrade bypass
            hookFirstMethodByCount(packageManagerCls, "checkDowngrade", 2) {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = null }
            }
            // Flyme checkDowngrade
            if (packageInfoLiteCls != null) {
                hookFirstMethod(packageManagerCls, "checkDowngrade", packageInfoLiteCls, packageInfoLiteCls) {
                    before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = true }
                }
            }
            // Digest bypass
            val strictJarVerifierCls = findHostClass("android.util.jar.StrictJarVerifier")
            hookFirstMethodAny(strictJarVerifierCls, "verifyMessageDigest") {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
            }
            hookFirstMethodAny(strictJarVerifierCls, "verify") {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
            }
            hookFirstMethodAny(findHostClass("java.security.MessageDigest"), "isEqual") {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
            }
            // AssetManager compressed table
            hookFirstMethodAny(findHostClass("android.content.res.AssetManager"), "containsAllocatedTable") {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = false }
            }
            // Min signature scheme
            val intType = Int::class.javaPrimitiveType!!
            val apkSignatureVerifierCls = findHostClass("android.util.apk.ApkSignatureVerifier")
            hookFirstMethod(apkSignatureVerifierCls, "getMinimumSignatureSchemeVersionForTargetSdk", intType) {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = 0 }
            }
            hookFirstMethod(
                findHostClass("com.android.apksig.ApkVerifier"),
                "getMinimumSignatureSchemeVersionForTargetSdk",
                intType,
            ) {
                before { if (readConf(confPrefs, ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = 0 }
            }

            // ── SharedUser: hasCommonAncestor bypass ──
            // INSTALL_FAILED_SHARED_USER_INCOMPATIBLE is thrown when signing lineage diverges
            val signingDetailsCls = findHostClass("android.content.pm.SigningDetails")
            if (signingDetailsCls != null) {
                hookFirstMethodAny(signingDetailsCls, "hasCommonAncestor") {
                    before {
                        if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)
                            && readConf(confPrefs, ModuleConfig.CORE_PATCH_SHARED_USER, false)
                        ) {
                            YLog.info("$TAG: hasCommonAncestor intercepted, returning true")
                            result = true
                        }
                    }
                }
            }

            // ── Deoptimize verifySignatures (required for stack trace checks) ──
            val pmsUtilsCls = findHostClass("com.android.server.pm.PackageManagerServiceUtils")
            if (pmsUtilsCls != null) {
                runCatching {
                    for (m in pmsUtilsCls.declaredMethods) {
                        if (m.name == "verifySignatures") {
                            deoptimizeMethod?.invoke(null, m)
                            YLog.info("$TAG: deopt verifySignatures success")
                        }
                    }
                }.onFailure { YLog.debug("$TAG: deopt verifySignatures skipped: ${it.message}") }
            }

            // ── SharedUser: removePackage / addPackage signature merge ──
            val sharedUserSettingCls = findHostClass("com.android.server.pm.SharedUserSetting")
            if (sharedUserSettingCls != null && readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_SHARED_USER, false)) {
                hookFirstMethodAny(sharedUserSettingCls, "removePackage") {
                    before {
                        val flags = XposedHelpers.getObjectField(instance, "uidFlags") as? Int ?: return@before
                        if (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) return@before
                        val toRemove = args[0] ?: return@before
                        val sharedUserSig = getSettingSigningDetails(instance)
                        var newSig: Any? = null
                        val packages = XposedHelpers.getObjectField(instance, "packages")
                        val size = XposedHelpers.callMethod(packages, "size") as Int
                        var removed = false
                        for (i in 0 until size) {
                            val p = XposedHelpers.callMethod(packages, "valueAt", i)
                            if (toRemove == p) { removed = true; continue }
                            val packageSig = getSettingSigningDetails(p)
                            if (callOriginMethod(packageSig, "checkCapability", sharedUserSig, 0) == true
                                || callOriginMethod(sharedUserSig, "checkCapability", packageSig, 0) == true) {
                                return@before
                            }
                            newSig = if (newSig == null) packageSig
                            else XposedHelpers.callMethod(newSig, "mergeLineageWith", packageSig, 2)
                        }
                        if (!removed || newSig == null) return@before
                        YLog.info("$TAG: updating signature in sharedUser during remove")
                        setSettingSigningDetails(instance, newSig)
                    }
                }
                hookFirstMethodAny(sharedUserSettingCls, "addPackage") {
                    before {
                        val flags = XposedHelpers.getObjectField(instance, "uidFlags") as? Int ?: return@before
                        if (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) return@before
                        val toAdd = args[0] ?: return@before
                        val sharedUserSig = getSettingSigningDetails(instance)
                        var newSig: Any? = null
                        val packages = XposedHelpers.getObjectField(instance, "packages")
                        val size = XposedHelpers.callMethod(packages, "size") as Int
                        var added = false
                        for (i in 0 until size) {
                            var p = XposedHelpers.callMethod(packages, "valueAt", i)
                            if (toAdd == p) { added = true; p = toAdd }
                            val packageSig = getSettingSigningDetails(p)
                            if (callOriginMethod(packageSig, "checkCapability", sharedUserSig, 0) == true
                                || callOriginMethod(sharedUserSig, "checkCapability", packageSig, 0) == true) {
                                return@before
                            }
                            newSig = if (newSig == null) packageSig
                            else XposedHelpers.callMethod(newSig, "mergeLineageWith", packageSig, 2)
                        }
                        if (!added || newSig == null) return@before
                        YLog.info("$TAG: updating signature in sharedUser during add $toAdd")
                        setSettingSigningDetails(instance, newSig)
                    }
                }
            }
        }
    }

    private fun getSettingSigningDetails(obj: Any): Any {
        return XposedHelpers.getObjectField(
            XposedHelpers.getObjectField(obj, "signatures"), "mSigningDetails"
        )
    }

    private fun setSettingSigningDetails(obj: Any, signingDetails: Any) {
        XposedHelpers.setObjectField(
            XposedHelpers.getObjectField(obj, "signatures"), "mSigningDetails", signingDetails
        )
    }

    companion object {
        fun callOriginMethod(obj: Any, methodName: String, vararg args: Any?): Any? {
            val m = XposedHelpers.findMethodBestMatch(obj::class.java, methodName, *args)
            return XposedBridge.invokeOriginalMethod(m, obj, args)
        }
    }
}
