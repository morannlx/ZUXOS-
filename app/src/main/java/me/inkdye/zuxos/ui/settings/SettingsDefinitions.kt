package me.inkdye.zuxos.ui.settings

import androidx.annotation.StringRes
import me.inkdye.zuxos.R

object SettingKeys {
    const val APP_INFO_EXTEND_ENABLED = "app_info_extend_enabled"
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
    const val HOOK_LOGGING_ENABLED = "hook_logging_enabled"
}

data class BooleanSettingSpec(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val defaultValue: Boolean,
)

val AppInfoFeatureSetting = BooleanSettingSpec(
    key = SettingKeys.APP_INFO_EXTEND_ENABLED,
    titleRes = R.string.app_info_feature_enabled,
    descRes = R.string.app_info_feature_enabled_desc,
    defaultValue = true,
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
    listOf(HookLoggingSetting, AppInfoFeatureSetting) + AppInfoDetailSettings + PackageInstallerFeatureSettings
