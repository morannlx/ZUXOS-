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

val ModuleBooleanSettings = listOf(AppInfoFeatureSetting) + AppInfoDetailSettings
