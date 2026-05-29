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

    const val PREFS_NAME = "xposed_module_config"
    private const val MODULE_PACKAGE = "me.inkdye.zuxos"
    private const val TAG = "ModuleConfig"

    const val PACKAGE_INSTALLER_DISABLE_SCAN = "disable_scanAPK"
    const val PACKAGE_INSTALLER_ALWAYS_ALLOW_PERMISSION = "Always_AllowPermission"
    const val PACKAGE_INSTALLER_SKIP_WARN_PAGE = "Skip_WarnPage"
    const val PACKAGE_INSTALLER_DISABLE_INSTALLER_AD = "disable_installerAD"
    const val PACKAGE_INSTALLER_STYLE_HOOK = "packageInstallerStyle_hook"
    const val PACKAGE_INSTALLER_DISABLE_DELETE = "package_installer_disable_delete"
    const val LENOVO_SYSTEM_UPDATE_HIDE_NEW_VERSION_REMINDER = "lenovo_system_update_hide_new_version_reminder"
    const val LENOVO_SYSTEM_UPDATE_FORCE_LOCAL_INSTALL = "lenovo_system_update_force_local_install"
    const val LENOVO_ABOUT_DEVICE_INFO_ENABLED = "lenovo_about_device_info_enabled"
    const val LENOVO_ABOUT_DEVICE_IMAGE_ENABLED = "lenovo_about_device_image_enabled"
    const val LENOVO_ABOUT_DEVICE_IMAGE_PATH = "lenovo_about_device_image_path"
    const val LENOVO_ABOUT_DEVICE_NAME = "lenovo_about_device_name"
    const val LENOVO_ABOUT_DEVICE_MODEL = "lenovo_about_device_model"
    const val LENOVO_ABOUT_DEVICE_PROCESSOR = "lenovo_about_device_processor"
    const val LENOVO_ABOUT_DEVICE_ROM = "lenovo_about_device_rom"
    const val LENOVO_ABOUT_DEVICE_RAM = "lenovo_about_device_ram"
    const val LENOVO_ABOUT_DEVICE_SOFTWARE_VERSION = "lenovo_about_device_software_version"
    const val LENOVO_ABOUT_DEVICE_ANDROID_VERSION = "lenovo_about_device_android_version"
    const val LENOVO_GAME_ASSISTANT_FORCE_DIRECT_FEATURES = "lenovo_game_assistant_force_direct_features"
    const val LENOVO_GAME_ASSISTANT_SPOOF_ELDEN = "lenovo_game_assistant_spoof_elden"
    const val LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT = "lenovo_game_assistant_force_voice_assistant"
    const val LENOVO_GAME_ASSISTANT_FORCE_BARRAGE = "lenovo_game_assistant_force_barrage"
    const val LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT = "lenovo_game_assistant_force_highlight"
    const val LENOVO_GAME_ASSISTANT_FORCE_ISLAND = "lenovo_game_assistant_force_island"
    const val LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION =
        "lenovo_game_assistant_force_prevent_misoperation"
    const val LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND =
        "lenovo_game_assistant_force_notification_dnd"
    const val LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD =
        "lenovo_game_assistant_force_intelligent_pre_download"
    const val LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING =
        "lenovo_game_assistant_force_intelligent_bypass_charging"
    const val LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE =
        "lenovo_game_assistant_force_mobile_network_accelerate"
    const val LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE =
        "lenovo_game_assistant_force_main_sim_accelerate"
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_ENABLED =
        "lenovo_systemui_control_center_enabled"
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR =
        "lenovo_systemui_control_center_tile_inactive_color"
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR =
        "lenovo_systemui_control_center_tile_active_color"
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA =
        "lenovo_systemui_control_center_tile_inactive_alpha"
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA =
        "lenovo_systemui_control_center_tile_active_alpha"
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA =
        "lenovo_systemui_control_center_panel_alpha"
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS =
        "lenovo_systemui_control_center_pull_down_blur_radius"
    const val HOOK_LOGGING_ENABLED = "hook_logging_enabled"
    const val DISABLE_FLAG_SECURE = "disable_flag_secure"
    const val CORE_PATCH_ENABLED = "core_patch_enabled"
    const val CORE_PATCH_DOWNGRADE = "downgrade"
    const val CORE_PATCH_AUTH_CREAK = "authcreak"
    const val CORE_PATCH_DIGEST_CREAK = "digestCreak"
    const val CORE_PATCH_EXACT_SIG_CHECK = "exactSigCheck"
    const val CORE_PATCH_USE_PRE_SIG = "UsePreSig"
    const val CORE_PATCH_BYPASS_BLOCK = "bypassBlock"
    const val CORE_PATCH_SHARED_USER = "sharedUser"
    const val CORE_PATCH_DISABLE_VERIFICATION_AGENT = "disableVerificationAgent"

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

    fun readInt(key: String, defaultValue: Int): Int {
        return try {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.reload()
            prefs.getInt(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read int config for key: $key", e)
            defaultValue
        }
    }

    fun readFloat(key: String, defaultValue: Float): Float {
        return try {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.reload()
            prefs.getFloat(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read float config for key: $key", e)
            defaultValue
        }
    }

    fun readString(key: String, defaultValue: String = ""): String {
        return try {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.reload()
            prefs.getString(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read string config for key: $key", e)
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

    @SuppressLint("WorldReadableFiles")
    fun saveInt(context: Context, key: String, value: Int) {
        try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.edit().putInt(key, value).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save int config: $key=$value", e)
        }
    }

    @SuppressLint("WorldReadableFiles")
    fun saveFloat(context: Context, key: String, value: Float) {
        try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.edit().putFloat(key, value).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save float config: $key=$value", e)
        }
    }

    @SuppressLint("WorldReadableFiles")
    fun saveString(context: Context, key: String, value: String) {
        try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.edit().putString(key, value).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save string config: $key=$value", e)
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

    fun loadInt(context: Context, key: String, defaultValue: Int): Int {
        return try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.getInt(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load int config for key: $key", e)
            defaultValue
        }
    }

    fun loadFloat(context: Context, key: String, defaultValue: Float): Float {
        return try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.getFloat(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load float config for key: $key", e)
            defaultValue
        }
    }

    fun loadString(context: Context, key: String, defaultValue: String = ""): String {
        return try {
            val moduleContext = context.createPackageContext(
                MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY
            )
            val prefs = moduleContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_WORLD_READABLE
            )
            prefs.getString(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load string config for key: $key", e)
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
