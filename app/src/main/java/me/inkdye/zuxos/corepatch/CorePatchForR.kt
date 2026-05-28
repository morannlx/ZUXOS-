package me.inkdye.zuxos.corepatch

import android.annotation.TargetApi
import android.os.Build
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.PrintWriter
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
            // Downgrade bypass
            "com.android.server.pm.PackageManagerService".hook {
                    injectMember {
                        method { name = "checkDowngrade"; paramCount = 2 }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = null }
                }
            }
            // Flyme checkDowngrade
            "com.android.server.pm.PackageManagerService".hook {
                    injectMember {
                        method { name = "checkDowngrade"; param("android.content.pm.PackageInfoLite".toClass(), "android.content.pm.PackageInfoLite".toClass()) }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_DOWNGRADE, true)) result = true }
                }
            }
            // Digest bypass
            "android.util.jar.StrictJarVerifier".hook {
                    injectMember {
                        method { name = "verifyMessageDigest" }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
                }
            }
            "android.util.jar.StrictJarVerifier".hook {
                    injectMember {
                        method { name = "verify" }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
                }
            }
            "java.security.MessageDigest".hook {
                    injectMember {
                        method { name = "isEqual" }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = true }
                }
            }
            // AssetManager compressed table
            "android.content.res.AssetManager".hook {
                    injectMember {
                        method { name = "containsAllocatedTable" }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_AUTH_CREAK, false)) result = false }
                }
            }
            // Min signature scheme
            "android.util.apk.ApkSignatureVerifier".hook {
                    injectMember {
                        method { name = "getMinimumSignatureSchemeVersionForTargetSdk"; param(Int::class.java) }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = 0 }
                }
            }
            "com.android.apksig.ApkVerifier".toClassOrNull()?.hook {
                    injectMember {
                        method { name = "getMinimumSignatureSchemeVersionForTargetSdk"; param(Int::class.java) }
                    beforeHook {  if (XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME).getBoolean(ModuleConfig.CORE_PATCH_AUTH_CREAK, true)) result = 0 }
                }
            }

            // ── SharedUser: hasCommonAncestor bypass ──
            // INSTALL_FAILED_SHARED_USER_INCOMPATIBLE is thrown when signing lineage diverges
            val signingDetailsCls = "android.content.pm.SigningDetails".toClassOrNull()
            val confPrefs = XSharedPreferences(BuildConfig.APPLICATION_ID, ModuleConfig.PREFS_NAME)
            if (signingDetailsCls != null) {
                signingDetailsCls.hook {
                    injectMember {
                        method { name = "hasCommonAncestor" }
                        beforeHook {
                            if (readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true)
                                && readConf(confPrefs, ModuleConfig.CORE_PATCH_SHARED_USER, false)
                            ) {
                                YLog.info("$TAG: hasCommonAncestor intercepted, returning true")
                                result = true
                            }
                        }
                    }
                }
            }

            // ── Deoptimize verifySignatures (required for stack trace checks) ──
            val pmsUtilsCls = "com.android.server.pm.PackageManagerServiceUtils".toClassOrNull()
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
            val sharedUserSettingCls = "com.android.server.pm.SharedUserSetting".toClassOrNull()
            if (sharedUserSettingCls != null && readConf(confPrefs, ModuleConfig.CORE_PATCH_DIGEST_CREAK, true) && readConf(confPrefs, ModuleConfig.CORE_PATCH_SHARED_USER, false)) {
                sharedUserSettingCls.hook {
                    injectMember {
                        method { name = "removePackage" }
                        beforeHook {
                            val flags = XposedHelpers.getObjectField(instance, "uidFlags") as? Int ?: return@beforeHook
                            if (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) return@beforeHook
                            val toRemove = args[0] ?: return@beforeHook
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
                                    return@beforeHook
                                }
                                newSig = if (newSig == null) packageSig
                                else XposedHelpers.callMethod(newSig, "mergeLineageWith", packageSig, 2)
                            }
                            if (!removed || newSig == null) return@beforeHook
                            YLog.info("$TAG: updating signature in sharedUser during remove")
                            setSettingSigningDetails(instance, newSig)
                        }
                    }
                }
                sharedUserSettingCls.hook {
                    injectMember {
                        method { name = "addPackage" }
                        beforeHook {
                            val flags = XposedHelpers.getObjectField(instance, "uidFlags") as? Int ?: return@beforeHook
                            if (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) return@beforeHook
                            val toAdd = args[0] ?: return@beforeHook
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
                                    return@beforeHook
                                }
                                newSig = if (newSig == null) packageSig
                                else XposedHelpers.callMethod(newSig, "mergeLineageWith", packageSig, 2)
                            }
                            if (!added || newSig == null) return@beforeHook
                            YLog.info("$TAG: updating signature in sharedUser during add $toAdd")
                            setSettingSigningDetails(instance, newSig)
                        }
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
