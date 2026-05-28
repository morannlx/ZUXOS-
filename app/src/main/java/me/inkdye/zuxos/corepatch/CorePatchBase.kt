package me.inkdye.zuxos.corepatch

import android.util.Log
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.BuildConfig
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Member

abstract class CorePatchBase(protected val prefs: XSharedPreferences) {

    companion object {
        const val TAG = "CorePatch"
        const val SIGNATURE = "308203c6308202aea003020102021426d148b7c65944abcf3a683b4c3dd3b139c4ec85300d06092a864886f70d01010b05003074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964301e170d3139303130323138353233385a170d3439303130323138353233385a3074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f696430820122300d06092a864886f70d01010105000382010f003082010a028201010087fcde48d9beaeba37b733a397ae586fb42b6c3f4ce758dc3ef1327754a049b58f738664ece587994f1c6362f98c9be5fe82c72177260c390781f74a10a8a6f05a6b5ca0c7c5826e15526d8d7f0e74f2170064896b0cf32634a388e1a975ed6bab10744d9b371cba85069834bf098f1de0205cdee8e715759d302a64d248067a15b9beea11b61305e367ac71b1a898bf2eec7342109c9c5813a579d8a1b3e6a3fe290ea82e27fdba748a663f73cca5807cff1e4ad6f3ccca7c02945926a47279d1159599d4ecf01c9d0b62e385c6320a7a1e4ddc9833f237e814b34024b9ad108a5b00786ea15593a50ca7987cbbdc203c096eed5ff4bf8a63d27d33ecc963990203010001a350304e300c0603551d13040530030101ff301d0603551d0e04160414a361efb002034d596c3a60ad7b0332012a16aee3301f0603551d23041830168014a361efb002034d596c3a60ad7b0332012a16aee3300d06092a864886f70d01010b0500038201010022ccb684a7a8706f3ee7c81d6750fd662bf39f84805862040b625ddf378eeefae5a4f1f283deea61a3c7f8e7963fd745415153a531912b82b596e7409287ba26fb80cedba18f22ae3d987466e1fdd88e440402b2ea2819db5392cadee501350e81b8791675ea1a2ed7ef7696dff273f13fb742bb9625fa12ce9c2cb0b7b3d94b21792f1252b1d9e4f7012cb341b62ff556e6864b40927e942065d8f0f51273fcda979b8832dd5562c79acf719de6be5aee2a85f89265b071bf38339e2d31041bc501d5e0c034ab1cd9c64353b10ee70b49274093d13f733eb9d3543140814c72f8e003f301c7a00b1872cc008ad55e26df2e8f07441002c4bcb7dc746745f0db"
    }

    protected fun logDebug(msg: String) { if (BuildConfig.DEBUG) Log.d(TAG, msg) }
    protected fun logInfo(msg: String) { Log.i(TAG, msg) }
    protected fun logWarn(msg: String) { Log.w(TAG, msg) }
    protected fun logError(msg: String, e: Throwable? = null) { Log.e(TAG, msg, e) }

    protected fun findClass(className: String, cl: ClassLoader?): Class<*>? = try {
        Class.forName(className, false, cl)
    } catch (e: Throwable) { logDebug("findClass fail: $className"); null }

    /** All subclasses define their hooks here. Called inside loadApp("android"). */
    open fun onHook(param: PackageParam) { /* override in subclass */ }

    /**
     * Reload XSharedPreferences from disk then read a boolean.
     * If reload fails, returns true (hooks still fire as safety net,
     * matching original CorePatch's ReturnConstant behavior).
     */
    protected fun readConf(prefs: XSharedPreferences, key: String, defValue: Boolean): Boolean {
        try {
            prefs.reload()
            return prefs.getBoolean(key, defValue)
        } catch (e: Throwable) {
            Log.w(TAG, "readConf failed for key=$key, defaulting to true", e)
            return true
        }
    }

    protected fun callOriginMethod(obj: Any, name: String, vararg args: Any?): Any? {
        val m = obj::class.java.methods.firstOrNull { it.name == name && it.parameterCount == args.size }
            ?: throw IllegalStateException("method $name not found")
        return XposedBridge.invokeOriginalMethod(m, obj, args)
    }

    protected fun deoptimizeMethod(c: Class<*>, n: String) {
        for (m in c.declaredMethods) {
            val dm = XposedBridge::class.java.getDeclaredMethod("deoptimizeMethod", Member::class.java)
            if (m.name == n) { dm.invoke(null, m); if (BuildConfig.DEBUG) YLog.debug("$TAG: deopt ${m.name}") }
        }
    }

    protected fun getObjectField(obj: Any, field: String): Any? {
        val f = obj::class.java.getField(field).apply { isAccessible = true }; return f.get(obj)
    }
    protected fun setObjectField(obj: Any, field: String, value: Any?) {
        val f = obj::class.java.getField(field).apply { isAccessible = true }; f.set(obj, value)
    }
    protected fun callMethod(obj: Any, name: String, vararg args: Any?): Any? {
        val m = obj::class.java.methods.firstOrNull { it.name == name && it.parameterCount == args.size }
            ?: return null; m.isAccessible = true; return m.invoke(obj, *args)
    }
}
