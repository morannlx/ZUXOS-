package me.inkdye.zuxos.hook.settings

import me.inkdye.zuxos.config.ModuleConfig

internal sealed interface SettingsHostItem {
    val key: String
    val title: String
    val summary: String
}

internal data class SettingsHostBooleanItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Boolean = false,
) : SettingsHostItem

internal data class SettingsHostTextItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: String = "",
) : SettingsHostItem

internal data class SettingsHostIntItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Int,
    val min: Int,
    val max: Int,
) : SettingsHostItem

internal data class SettingsHostFloatItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Float,
    val min: Float,
    val max: Float,
) : SettingsHostItem

internal data class SettingsHostColorItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Int,
) : SettingsHostItem

internal data class SettingsHostPage(
    val id: String,
    val title: String,
    val items: List<SettingsHostItem>,
)

internal object SettingsHostDefinitions {
    const val MODULE_TITLE = "ZUXOS+"
    const val HOME_ENTRY_KEY = "top_level_zuxos_plus"
    const val HOST_PAGE_MARKER = "me.inkdye.zuxos.SHOW_SETTINGS_HOST_PAGE"
    const val HOST_PAGE_ID = "me.inkdye.zuxos.HOST_PAGE_ID"
    const val ROOT_PAGE_ID = "root"

    val pages = listOf(
        SettingsHostPage(
            id = "framework",
            title = "系统框架",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.DISABLE_FLAG_SECURE, "强制允许截屏"),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_ENABLED, "开启核心破解"),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_DOWNGRADE, "允许降级安装", defaultValue = true),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_AUTH_CREAK, "禁用 APK 签名验证"),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_DIGEST_CREAK, "绕过签名能力检查", defaultValue = true),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_EXACT_SIG_CHECK, "绕过精确签名匹配"),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_USE_PRE_SIG, "使用历史签名"),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_BYPASS_BLOCK, "绕过厂商安装黑名单", defaultValue = true),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_SHARED_USER, "绕过 sharedUserId 签名检查"),
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_DISABLE_VERIFICATION_AGENT, "禁用安装包验证代理", defaultValue = true),
            ),
        ),
        SettingsHostPage(
            id = "settings",
            title = "系统页面",
            items = listOf(
                SettingsHostBooleanItem("app_info_show_package_name", "显示包名"),
                SettingsHostBooleanItem("app_info_show_sdk_version", "显示 SDK 版本"),
                SettingsHostBooleanItem("app_info_show_install_time", "显示首次安装时间"),
                SettingsHostBooleanItem("app_info_show_update_time", "显示最后更新时间"),
                SettingsHostBooleanItem("app_info_show_install_source", "显示安装来源"),
                SettingsHostBooleanItem("app_info_enable_long_copy", "启用长按复制"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_ABOUT_DEVICE_INFO_ENABLED, "启用关于设备自定义"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_ENABLED, "替换设备图片"),
                SettingsHostTextItem(ModuleConfig.LENOVO_ABOUT_DEVICE_NAME, "设备名称", defaultValue = "Lenovo Xiaoxin Pad Pro"),
                SettingsHostTextItem(ModuleConfig.LENOVO_ABOUT_DEVICE_MODEL, "设备型号", defaultValue = "TB372FC"),
                SettingsHostTextItem(ModuleConfig.LENOVO_ABOUT_DEVICE_PROCESSOR, "处理器", defaultValue = "Snapdragon 8+ Gen 1"),
                SettingsHostTextItem(ModuleConfig.LENOVO_ABOUT_DEVICE_ROM, "机身存储", defaultValue = "256 GB / 512 GB"),
                SettingsHostTextItem(ModuleConfig.LENOVO_ABOUT_DEVICE_RAM, "运行内存", defaultValue = "12 GB"),
                SettingsHostTextItem(ModuleConfig.LENOVO_ABOUT_DEVICE_SOFTWARE_VERSION, "软件版本", defaultValue = "ZUXOS 16.0.0"),
                SettingsHostTextItem(ModuleConfig.LENOVO_ABOUT_DEVICE_ANDROID_VERSION, "Android 版本", defaultValue = "15"),
            ),
        ),
        SettingsHostPage(
            id = "lenovo_update",
            title = "系统更新",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LENOVO_SYSTEM_UPDATE_HIDE_NEW_VERSION_REMINDER, "隐藏新版本提醒"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_SYSTEM_UPDATE_FORCE_LOCAL_INSTALL, "强制显示本地安装入口"),
            ),
        ),
        SettingsHostPage(
            id = "game_assistant",
            title = "游戏助手",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN, "使用拯救者系列游戏助手"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT, "语音助手 / 变声器"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_BARRAGE, "弹幕"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT, "高光"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_ISLAND, "游戏岛"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION, "防误触"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND, "通知免打扰"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD, "智能预下载"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING, "智能旁路充电"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE, "移动网络加速"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE, "主卡加速"),
            ),
        ),
        SettingsHostPage(
            id = "systemui",
            title = "系统界面",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_ENABLED, "控制中心自定义"),
                SettingsHostColorItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR, "未激活磁贴颜色", defaultValue = 0xFFFFFFFF.toInt()),
                SettingsHostColorItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR, "激活磁贴颜色", defaultValue = 0xFF5FAE8B.toInt()),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA, "未激活磁贴透明度", defaultValue = 0.35f, min = 0f, max = 1f),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA, "激活磁贴透明度", defaultValue = 0.92f, min = 0f, max = 1f),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA, "面板透明度", defaultValue = 0.9f, min = 0f, max = 1f),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS, "下拉背景模糊半径", defaultValue = 18f, min = 0f, max = 200f),
            ),
        ),
        SettingsHostPage(
            id = "launcher",
            title = "桌面",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_HOTSEAT_EXPAND, "更大的 Dock 栏"),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_CUSTOM_GRID, "自定义桌面网格"),
                SettingsHostIntItem(ModuleConfig.LAUNCHER_CUSTOM_GRID_ROWS, "行数", defaultValue = 4, min = 3, max = 12),
                SettingsHostIntItem(ModuleConfig.LAUNCHER_CUSTOM_GRID_COLUMNS, "列数", defaultValue = 6, min = 3, max = 12),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_DISABLE_FORCE_STOP, "原生后台管理"),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_FORCE_STOP_WHITELIST_ENABLE, "白名单模式"),
                SettingsHostTextItem(ModuleConfig.LAUNCHER_FORCE_STOP_WHITELIST, "受保护包名", "多个包名使用英文逗号分隔"),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_RECENTS_MEMORY, "最近任务显示内存"),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_HIDE_ICON_LABEL, "隐藏桌面图标名称"),
            ),
        ),
        SettingsHostPage(
            id = "package_installer",
            title = "软件包安装程序",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_DISABLE_SCAN, "跳过安全扫描"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_ALWAYS_ALLOW_PERMISSION, "总是给予应用权限"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_SKIP_WARN_PAGE, "跳过未上架警告页"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_DISABLE_INSTALLER_AD, "去广告"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_STYLE_HOOK, "启用原生安装器"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_DISABLE_DELETE, "不要删除安装包"),
            ),
        ),
        SettingsHostPage(
            id = "module",
            title = "模块",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.HOOK_LOGGING_ENABLED, "启用 Hook 日志"),
                SettingsHostBooleanItem(ModuleConfig.SETTINGS_HOME_ENTRY_ENABLED, "在设置中显示入口", defaultValue = true),
            ),
        ),
    )

    fun page(id: String): SettingsHostPage? = pages.firstOrNull { it.id == id }
}
