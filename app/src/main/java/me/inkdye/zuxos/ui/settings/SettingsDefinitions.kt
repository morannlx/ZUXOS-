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
    ) +
        AppInfoDetailSettings + PackageInstallerFeatureSettings +
        LenovoSystemUpdateFeatureSettings + CorePatchFeatureSettings
