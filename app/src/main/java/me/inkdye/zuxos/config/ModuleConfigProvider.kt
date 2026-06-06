package me.inkdye.zuxos.config

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.util.Log
import me.inkdye.zuxos.BuildConfig

class ModuleConfigProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (!isTrustedCaller()) {
            Log.w(TAG, "Reject config call from uid=${Binder.getCallingUid()}")
            return null
        }
        val context = context ?: return null
        val key = arg ?: return null
        return when (method) {
            METHOD_GET_BOOLEAN -> Bundle().apply {
                val defaultValue = extras?.getBoolean(EXTRA_DEFAULT_VALUE, false) ?: false
                putBoolean(EXTRA_VALUE, ModuleConfig.loadBoolean(context, key, defaultValue))
            }
            METHOD_SET_BOOLEAN -> {
                ModuleConfig.saveBoolean(context, key, extras?.getBoolean(EXTRA_VALUE, false) ?: false)
                Bundle.EMPTY
            }
            METHOD_GET_STRING -> Bundle().apply {
                val defaultValue = extras?.getString(EXTRA_DEFAULT_VALUE).orEmpty()
                putString(EXTRA_VALUE, ModuleConfig.loadString(context, key, defaultValue))
            }
            METHOD_SET_STRING -> {
                ModuleConfig.saveString(context, key, extras?.getString(EXTRA_VALUE).orEmpty())
                Bundle.EMPTY
            }
            METHOD_GET_FLOAT -> Bundle().apply {
                val defaultValue = extras?.getFloat(EXTRA_DEFAULT_VALUE, 0f) ?: 0f
                putFloat(EXTRA_VALUE, ModuleConfig.loadFloat(context, key, defaultValue))
            }
            METHOD_SET_FLOAT -> {
                ModuleConfig.saveFloat(context, key, extras?.getFloat(EXTRA_VALUE, 0f) ?: 0f)
                Bundle.EMPTY
            }
            METHOD_GET_INT -> Bundle().apply {
                val defaultValue = extras?.getInt(EXTRA_DEFAULT_VALUE, 0) ?: 0
                putInt(EXTRA_VALUE, ModuleConfig.loadInt(context, key, defaultValue))
            }
            METHOD_SET_INT -> {
                ModuleConfig.saveInt(context, key, extras?.getInt(EXTRA_VALUE, 0) ?: 0)
                Bundle.EMPTY
            }
            else -> super.call(method, arg, extras)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun getType(uri: Uri): String? = null

    private fun isTrustedCaller(): Boolean {
        val context = context ?: return false
        val uid = Binder.getCallingUid()
        if (uid == android.os.Process.myUid()) return true
        val packages = context.packageManager.getPackagesForUid(uid).orEmpty()
        return BuildConfig.APPLICATION_ID in packages || PACKAGE_SETTINGS in packages
    }

    companion object {
        private const val TAG = "ZUXOS.ConfigProvider"
        private const val PACKAGE_SETTINGS = "com.android.settings"
        const val METHOD_GET_BOOLEAN = "getBoolean"
        const val METHOD_SET_BOOLEAN = "setBoolean"
        const val METHOD_GET_STRING = "getString"
        const val METHOD_SET_STRING = "setString"
        const val METHOD_GET_FLOAT = "getFloat"
        const val METHOD_SET_FLOAT = "setFloat"
        const val METHOD_GET_INT = "getInt"
        const val METHOD_SET_INT = "setInt"
        const val EXTRA_VALUE = "value"
        const val EXTRA_DEFAULT_VALUE = "defaultValue"

        val URI: Uri = Uri.parse("content://${BuildConfig.APPLICATION_ID}.config")
    }
}
