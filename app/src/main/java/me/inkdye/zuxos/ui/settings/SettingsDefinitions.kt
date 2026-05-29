package me.inkdye.zuxos.ui.settings

import androidx.annotation.StringRes
import me.inkdye.zuxos.R
import me.inkdye.zuxos.config.ModuleConfig

object SettingKeys {
    const val APP_INFO_SHOW_PACKAGE_NAME = "app_info_show_package_name"
    const val APP_INFO_SHOW_SDK_VERSION = "app_info_show_sdk_version"
    const val APP_INFO_SHOW_INSTALL_TIME = "app_info_show_install_time"
    const val APP_INFO_SHOW_UPDATE_TIME = "app_info_show_update_time"
    const val APP_INFO_SHOW_INSTALL_SOURCE = "app_info_show_install_source"
    const val APP_INFO_ENABLE_LONG_COPY = "app_info_enable_long_copy"
    const val PACKAGE_INSTALLER_DISABLE_SCAN = "disable_scanAPK"
    const val PACKAGE_INSTALLER_ALWAYS_ALLOW_PERMISSION = "Always_AllowPermission"
    const val PACKAGE_INSTALLER_SKIP_WARN_PAGE = "Skip_WarnPage"
    const val PACKAGE_INSTALLER_DISABLE_INSTALLER_AD = "disable_installerAD"
    const val PACKAGE_INSTALLER_STYLE_HOOK = "packageInstallerStyle_hook"
    const val PACKAGE_INSTALLER_DISABLE_DELETE = "package_installer_disable_delete"
    const val LENOVO_SYSTEM_UPDATE_HIDE_NEW_VERSION_REMINDER =
        ModuleConfig.LENOVO_SYSTEM_UPDATE_HIDE_NEW_VERSION_REMINDER
    const val LENOVO_SYSTEM_UPDATE_FORCE_LOCAL_INSTALL =
        ModuleConfig.LENOVO_SYSTEM_UPDATE_FORCE_LOCAL_INSTALL
    const val LENOVO_ABOUT_DEVICE_INFO_ENABLED =
        ModuleConfig.LENOVO_ABOUT_DEVICE_INFO_ENABLED
    const val LENOVO_ABOUT_DEVICE_IMAGE_ENABLED =
        ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_ENABLED
    const val LENOVO_ABOUT_DEVICE_IMAGE_PATH =
        ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH
    const val LENOVO_ABOUT_DEVICE_NAME =
        ModuleConfig.LENOVO_ABOUT_DEVICE_NAME
    const val LENOVO_ABOUT_DEVICE_MODEL =
        ModuleConfig.LENOVO_ABOUT_DEVICE_MODEL
    const val LENOVO_ABOUT_DEVICE_PROCESSOR =
        ModuleConfig.LENOVO_ABOUT_DEVICE_PROCESSOR
    const val LENOVO_ABOUT_DEVICE_ROM =
        ModuleConfig.LENOVO_ABOUT_DEVICE_ROM
    const val LENOVO_ABOUT_DEVICE_RAM =
        ModuleConfig.LENOVO_ABOUT_DEVICE_RAM
    const val LENOVO_ABOUT_DEVICE_SOFTWARE_VERSION =
        ModuleConfig.LENOVO_ABOUT_DEVICE_SOFTWARE_VERSION
    const val LENOVO_ABOUT_DEVICE_ANDROID_VERSION =
        ModuleConfig.LENOVO_ABOUT_DEVICE_ANDROID_VERSION
    const val LENOVO_GAME_ASSISTANT_FORCE_DIRECT_FEATURES =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_DIRECT_FEATURES
    const val LENOVO_GAME_ASSISTANT_SPOOF_ELDEN =
        ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN
    const val LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT
    const val LENOVO_GAME_ASSISTANT_FORCE_BARRAGE =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_BARRAGE
    const val LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT
    const val LENOVO_GAME_ASSISTANT_FORCE_ISLAND =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_ISLAND
    const val LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION
    const val LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND
    const val LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD
    const val LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING
    const val LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE
    const val LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE =
        ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_ENABLED =
        ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_ENABLED
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR =
        ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR =
        ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA =
        ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA =
        ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA =
        ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA
    const val LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS =
        ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS
    const val HOOK_LOGGING_ENABLED = "hook_logging_enabled"
    const val DISABLE_FLAG_SECURE = ModuleConfig.DISABLE_FLAG_SECURE
    const val CORE_PATCH_ENABLED = ModuleConfig.CORE_PATCH_ENABLED
    const val CORE_PATCH_DOWNGRADE = ModuleConfig.CORE_PATCH_DOWNGRADE
    const val CORE_PATCH_AUTH_CREAK = ModuleConfig.CORE_PATCH_AUTH_CREAK
    const val CORE_PATCH_DIGEST_CREAK = ModuleConfig.CORE_PATCH_DIGEST_CREAK
    const val CORE_PATCH_EXACT_SIG_CHECK = ModuleConfig.CORE_PATCH_EXACT_SIG_CHECK
    const val CORE_PATCH_USE_PRE_SIG = ModuleConfig.CORE_PATCH_USE_PRE_SIG
    const val CORE_PATCH_BYPASS_BLOCK = ModuleConfig.CORE_PATCH_BYPASS_BLOCK
    const val CORE_PATCH_SHARED_USER = ModuleConfig.CORE_PATCH_SHARED_USER
    const val CORE_PATCH_DISABLE_VERIFICATION_AGENT = ModuleConfig.CORE_PATCH_DISABLE_VERIFICATION_AGENT
}

data class BooleanSettingSpec(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val defaultValue: Boolean,
)

data class FloatSettingSpec(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val defaultValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>,
    val steps: Int = 0,
)

data class ColorSettingSpec(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val defaultValue: Int,
)

data class TextSettingSpec(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val defaultValue: String,
)

val HookLoggingSetting = BooleanSettingSpec(
    key = SettingKeys.HOOK_LOGGING_ENABLED,
    titleRes = R.string.hook_logging_enabled,
    descRes = R.string.hook_logging_enabled_desc,
    defaultValue = false,
)

val PackageInstallerFeatureSettings = listOf(
    BooleanSettingSpec(
        SettingKeys.PACKAGE_INSTALLER_DISABLE_SCAN,
        R.string.package_installer_disable_scan_title,
        R.string.package_installer_disable_scan_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.PACKAGE_INSTALLER_ALWAYS_ALLOW_PERMISSION,
        R.string.package_installer_always_allow_permission_title,
        R.string.package_installer_always_allow_permission_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.PACKAGE_INSTALLER_SKIP_WARN_PAGE,
        R.string.package_installer_skip_warn_page_title,
        R.string.package_installer_skip_warn_page_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.PACKAGE_INSTALLER_DISABLE_INSTALLER_AD,
        R.string.package_installer_disable_installer_ad_title,
        R.string.package_installer_disable_installer_ad_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.PACKAGE_INSTALLER_STYLE_HOOK,
        R.string.package_installer_style_title,
        R.string.package_installer_style_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.PACKAGE_INSTALLER_DISABLE_DELETE,
        R.string.package_installer_disable_delete_title,
        R.string.package_installer_disable_delete_desc,
        false,
    ),
)

val LenovoSystemUpdateReminderSetting = BooleanSettingSpec(
    key = SettingKeys.LENOVO_SYSTEM_UPDATE_HIDE_NEW_VERSION_REMINDER,
    titleRes = R.string.lenovo_system_update_hide_new_version_reminder_title,
    descRes = R.string.lenovo_system_update_hide_new_version_reminder_desc,
    defaultValue = false,
)

val LenovoSystemUpdateLocalInstallSetting = BooleanSettingSpec(
    key = SettingKeys.LENOVO_SYSTEM_UPDATE_FORCE_LOCAL_INSTALL,
    titleRes = R.string.lenovo_system_update_force_local_install_title,
    descRes = R.string.lenovo_system_update_force_local_install_desc,
    defaultValue = false,
)

val LenovoSystemUpdateFeatureSettings = listOf(
    LenovoSystemUpdateReminderSetting,
    LenovoSystemUpdateLocalInstallSetting,
)

val LenovoAboutDeviceMasterSetting = BooleanSettingSpec(
    key = SettingKeys.LENOVO_ABOUT_DEVICE_INFO_ENABLED,
    titleRes = R.string.lenovo_about_device_enabled_title,
    descRes = R.string.lenovo_about_device_enabled_desc,
    defaultValue = false,
)

val LenovoAboutDeviceImageSetting = BooleanSettingSpec(
    key = SettingKeys.LENOVO_ABOUT_DEVICE_IMAGE_ENABLED,
    titleRes = R.string.lenovo_about_device_image_enabled_title,
    descRes = R.string.lenovo_about_device_image_enabled_desc,
    defaultValue = false,
)

val LenovoAboutDeviceTextSettings = listOf(
    TextSettingSpec(
        SettingKeys.LENOVO_ABOUT_DEVICE_NAME,
        R.string.lenovo_about_device_name_title,
        R.string.lenovo_about_device_name_desc,
        "Lenovo Xiaoxin Pad Pro",
    ),
    TextSettingSpec(
        SettingKeys.LENOVO_ABOUT_DEVICE_MODEL,
        R.string.lenovo_about_device_model_title,
        R.string.lenovo_about_device_model_desc,
        "TB372FC",
    ),
    TextSettingSpec(
        SettingKeys.LENOVO_ABOUT_DEVICE_PROCESSOR,
        R.string.lenovo_about_device_processor_title,
        R.string.lenovo_about_device_processor_desc,
        "Snapdragon 8+ Gen 1",
    ),
    TextSettingSpec(
        SettingKeys.LENOVO_ABOUT_DEVICE_ROM,
        R.string.lenovo_about_device_rom_title,
        R.string.lenovo_about_device_rom_desc,
        "256 GB / 512 GB",
    ),
    TextSettingSpec(
        SettingKeys.LENOVO_ABOUT_DEVICE_RAM,
        R.string.lenovo_about_device_ram_title,
        R.string.lenovo_about_device_ram_desc,
        "12 GB",
    ),
    TextSettingSpec(
        SettingKeys.LENOVO_ABOUT_DEVICE_SOFTWARE_VERSION,
        R.string.lenovo_about_device_software_version_title,
        R.string.lenovo_about_device_software_version_desc,
        "ZUXOS 16.0.0",
    ),
    TextSettingSpec(
        SettingKeys.LENOVO_ABOUT_DEVICE_ANDROID_VERSION,
        R.string.lenovo_about_device_android_version_title,
        R.string.lenovo_about_device_android_version_desc,
        "15",
    ),
)

val LenovoGameAssistantFeatureSettings = listOf(
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN,
        R.string.lenovo_game_assistant_spoof_elden_title,
        R.string.lenovo_game_assistant_spoof_elden_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT,
        R.string.lenovo_game_assistant_force_voice_assistant_title,
        R.string.lenovo_game_assistant_force_voice_assistant_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_BARRAGE,
        R.string.lenovo_game_assistant_force_barrage_title,
        R.string.lenovo_game_assistant_force_barrage_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT,
        R.string.lenovo_game_assistant_force_highlight_title,
        R.string.lenovo_game_assistant_force_highlight_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_ISLAND,
        R.string.lenovo_game_assistant_force_island_title,
        R.string.lenovo_game_assistant_force_island_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION,
        R.string.lenovo_game_assistant_force_prevent_misoperation_title,
        R.string.lenovo_game_assistant_force_prevent_misop_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND,
        R.string.lenovo_game_assistant_force_notification_dnd_title,
        R.string.lenovo_game_assistant_force_notification_dnd_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD,
        R.string.lenovo_game_assistant_force_intelligent_pre_download_title,
        R.string.lenovo_game_assistant_force_intelligent_pre_download_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING,
        R.string.lenovo_game_assistant_force_intelligent_bypass_charging_title,
        R.string.lenovo_game_assistant_force_intelligent_bypass_charging_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE,
        R.string.lenovo_game_assistant_force_mobile_network_accelerate_title,
        R.string.lenovo_game_assistant_force_mobile_network_accelerate_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE,
        R.string.lenovo_game_assistant_force_main_sim_accelerate_title,
        R.string.lenovo_game_assistant_force_main_sim_accelerate_desc,
        false,
    ),
)

val LenovoSystemUIControlCenterMasterSetting = BooleanSettingSpec(
    key = SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_ENABLED,
    titleRes = R.string.lenovo_systemui_control_center_enabled_title,
    descRes = R.string.lenovo_systemui_control_center_enabled_desc,
    defaultValue = false,
)

val LenovoSystemUIControlCenterColorSettings = listOf(
    ColorSettingSpec(
        SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR,
        R.string.lenovo_systemui_control_center_tile_inactive_color_title,
        R.string.lenovo_systemui_control_center_tile_inactive_color_desc,
        0xFFFFFFFF.toInt(),
    ),
    ColorSettingSpec(
        SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR,
        R.string.lenovo_systemui_control_center_tile_active_color_title,
        R.string.lenovo_systemui_control_center_tile_active_color_desc,
        0xFF5FAE8B.toInt(),
    ),
)

val LenovoSystemUIControlCenterFloatSettings = listOf(
    FloatSettingSpec(
        SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA,
        R.string.lenovo_systemui_control_center_tile_inactive_alpha_title,
        R.string.lenovo_systemui_control_center_tile_inactive_alpha_desc,
        0.35f,
        0f..1f,
        19,
    ),
    FloatSettingSpec(
        SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA,
        R.string.lenovo_systemui_control_center_tile_active_alpha_title,
        R.string.lenovo_systemui_control_center_tile_active_alpha_desc,
        0.92f,
        0f..1f,
        19,
    ),
    FloatSettingSpec(
        SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA,
        R.string.lenovo_systemui_control_center_panel_alpha_title,
        R.string.lenovo_systemui_control_center_panel_alpha_desc,
        0.9f,
        0f..1f,
        19,
    ),
    FloatSettingSpec(
        SettingKeys.LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS,
        R.string.lenovo_systemui_control_center_pull_down_blur_radius_title,
        R.string.lenovo_systemui_control_center_pull_down_blur_radius_desc,
        18f,
        0f..200f,
        39,
    ),
)

val DisableFlagSecureSetting = BooleanSettingSpec(
    key = SettingKeys.DISABLE_FLAG_SECURE,
    titleRes = R.string.disable_flag_secure_title,
    descRes = R.string.disable_flag_secure_desc,
    defaultValue = false,
)

val CorePatchMasterSetting = BooleanSettingSpec(
    key = SettingKeys.CORE_PATCH_ENABLED,
    titleRes = R.string.core_patch_master_title,
    descRes = R.string.core_patch_master_desc,
    defaultValue = false,
)

val CorePatchFeatureSettings = listOf(
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_DOWNGRADE,
        R.string.core_patch_downgrade_title,
        R.string.core_patch_downgrade_desc,
        true,
    ),
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_AUTH_CREAK,
        R.string.core_patch_auth_creak_title,
        R.string.core_patch_auth_creak_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_DIGEST_CREAK,
        R.string.core_patch_digest_creak_title,
        R.string.core_patch_digest_creak_desc,
        true,
    ),
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_EXACT_SIG_CHECK,
        R.string.core_patch_exact_sig_check_title,
        R.string.core_patch_exact_sig_check_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_USE_PRE_SIG,
        R.string.core_patch_use_pre_sig_title,
        R.string.core_patch_use_pre_sig_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_BYPASS_BLOCK,
        R.string.core_patch_bypass_block_title,
        R.string.core_patch_bypass_block_desc,
        true,
    ),
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_SHARED_USER,
        R.string.core_patch_shared_user_title,
        R.string.core_patch_shared_user_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.CORE_PATCH_DISABLE_VERIFICATION_AGENT,
        R.string.core_patch_disable_verification_agent_title,
        R.string.core_patch_disable_verification_agent_desc,
        true,
    ),
)

val AppInfoDetailSettings = listOf(
    BooleanSettingSpec(
        SettingKeys.APP_INFO_SHOW_PACKAGE_NAME,
        R.string.app_info_show_package_name,
        R.string.app_info_show_package_name_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.APP_INFO_SHOW_SDK_VERSION,
        R.string.app_info_show_sdk_version,
        R.string.app_info_show_sdk_version_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.APP_INFO_SHOW_INSTALL_TIME,
        R.string.app_info_show_install_time,
        R.string.app_info_show_install_time_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.APP_INFO_SHOW_UPDATE_TIME,
        R.string.app_info_show_update_time,
        R.string.app_info_show_update_time_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.APP_INFO_SHOW_INSTALL_SOURCE,
        R.string.app_info_show_install_source,
        R.string.app_info_show_install_source_desc,
        false,
    ),
    BooleanSettingSpec(
        SettingKeys.APP_INFO_ENABLE_LONG_COPY,
        R.string.app_info_enable_long_copy,
        R.string.app_info_enable_long_copy_desc,
        false,
    ),
)

val ModuleBooleanSettings =
    listOf(
        HookLoggingSetting,
        DisableFlagSecureSetting,
        CorePatchMasterSetting,
        LenovoAboutDeviceMasterSetting,
        LenovoAboutDeviceImageSetting,
        LenovoSystemUIControlCenterMasterSetting,
    ) +
        AppInfoDetailSettings + PackageInstallerFeatureSettings +
        LenovoSystemUpdateFeatureSettings + LenovoGameAssistantFeatureSettings +
        CorePatchFeatureSettings

val ModuleFloatSettings = LenovoSystemUIControlCenterFloatSettings

val ModuleColorSettings = LenovoSystemUIControlCenterColorSettings

val ModuleTextSettings = LenovoAboutDeviceTextSettings
