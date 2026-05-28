package me.inkdye.zuxos.config

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import de.robv.android.xposed.XSharedPreferences

/**
 * 模块配置管理（参照 ZUX-ZTool 实现）
 *
 * 使用独立 SharedPreferences 文件名，通过 createPackageContext + MODE_WORLD_READABLE
 * 实现跨进程配置共享，兼容 Android 12+。
 *
 * 写入：App 进程通过 createPackageContext 写入
 * 读取：Hook 进程通过 XSharedPreferences 读取
 */
object ModuleConfig {

    private const val PREFS_NAME = "xposed_module_config"
    private const val MODULE_PACKAGE = "me.inkdye.zuxos"
    private const val TAG = "ModuleConfig"

    const val PACKAGE_INSTALLER_DISABLE_SCAN = "disable_scanAPK"
    const val PACKAGE_INSTALLER_ALWAYS_ALLOW_PERMISSION = "Always_AllowPermission"
    const val PACKAGE_INSTALLER_SKIP_WARN_PAGE = "Skip_WarnPage"
    const val PACKAGE_INSTALLER_DISABLE_INSTALLER_AD = "disable_installerAD"
    const val PACKAGE_INSTALLER_STYLE_HOOK = "packageInstallerStyle_hook"
    const val PACKAGE_INSTALLER_DISABLE_DELETE = "package_installer_disable_delete"
    const val HOOK_LOGGING_ENABLED = "hook_logging_enabled"

    /**
     * 读取配置（Hook 进程调用）
     */
    fun isEnabled(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.reload()
            prefs.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read config for key: $key", e)
            defaultValue
        }
    }

    /**
     * 写入配置（App 进程调用）
     */
    @SuppressLint("WorldReadableFiles")
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.edit().putBoolean(key, value).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save config: $key=$value", e)
        }
    }

    /**
     * 读取配置（App 进程调用，用于 UI 初始化）
     */
    fun loadBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load config for key: $key", e)
            defaultValue
        }
    }

    fun contains(context: Context, key: String): Boolean {
        return try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.contains(key)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check config key: $key", e)
            false
        }
    }
}
