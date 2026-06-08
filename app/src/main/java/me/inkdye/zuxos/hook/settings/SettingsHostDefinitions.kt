package me.inkdye.zuxos.hook.settings

import me.inkdye.zuxos.config.ModuleConfig

internal sealed interface SettingsHostItem {
    val key: String
    val title: String
    val summary: String
    val visibleWhen: List<SettingsHostVisibility>
}

internal data class SettingsHostVisibility(
    val key: String,
    val defaultValue: Boolean = false,
    val expectedValue: Boolean = true,
)

internal data class SettingsHostMenuItem(
    override val key: String,
    override val title: String,
    val pageId: String,
    override val summary: String = "",
    override val visibleWhen: List<SettingsHostVisibility> = emptyList(),
) : SettingsHostItem

internal data class SettingsHostBooleanItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Boolean = false,
    override val visibleWhen: List<SettingsHostVisibility> = emptyList(),
) : SettingsHostItem

internal data class SettingsHostTextItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: String = "",
    override val visibleWhen: List<SettingsHostVisibility> = emptyList(),
) : SettingsHostItem

internal data class SettingsHostIntItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Int,
    val min: Int,
    val max: Int,
    override val visibleWhen: List<SettingsHostVisibility> = emptyList(),
) : SettingsHostItem

internal data class SettingsHostFloatItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Float,
    val min: Float,
    val max: Float,
    override val visibleWhen: List<SettingsHostVisibility> = emptyList(),
) : SettingsHostItem

internal data class SettingsHostColorItem(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val defaultValue: Int,
    override val visibleWhen: List<SettingsHostVisibility> = emptyList(),
) : SettingsHostItem

internal data class SettingsHostPage(
    val id: String,
    val title: String,
    val summary: String = "",
    val items: List<SettingsHostItem>,
)

internal object SettingsHostDefinitions {
    const val MODULE_TITLE = "ZUXOS+"
    const val HOME_ENTRY_KEY = "top_level_zuxos_plus"
    const val HOST_PAGE_MARKER = "me.inkdye.zuxos.SHOW_SETTINGS_HOST_PAGE"
    const val HOST_PAGE_ID = "me.inkdye.zuxos.HOST_PAGE_ID"
    const val ROOT_PAGE_ID = "root"

    private const val PAGE_FRAMEWORK = "framework"
    private const val PAGE_CORE_PATCH = "core_patch"
    private const val PAGE_SYSTEM = "system"
    private const val PAGE_APP_INFO = "app_info"
    private const val PAGE_ABOUT_DEVICE = "about_device"
    private const val PAGE_LENOVO_UPDATE = "lenovo_update"
    private const val PAGE_GAME_ASSISTANT = "game_assistant"
    private const val PAGE_SYSTEMUI = "systemui"
    private const val PAGE_CONTROL_CENTER = "control_center"
    private const val PAGE_LAUNCHER = "launcher"
    private const val PAGE_PACKAGE_INSTALLER = "package_installer"
    private const val PAGE_GENERAL = "general"

    val pages = listOf(
        SettingsHostPage(
            id = ROOT_PAGE_ID,
            title = MODULE_TITLE,
            items = listOf(
                SettingsHostMenuItem("zuxos_page_framework", "系统框架", PAGE_FRAMEWORK),
                SettingsHostMenuItem("zuxos_page_system", "系统设置", PAGE_SYSTEM),
                SettingsHostMenuItem("zuxos_page_lenovo_update", "系统更新", PAGE_LENOVO_UPDATE),
                SettingsHostMenuItem("zuxos_page_game_assistant", "游戏助手", PAGE_GAME_ASSISTANT),
                SettingsHostMenuItem("zuxos_page_systemui", "系统界面", PAGE_SYSTEMUI),
                SettingsHostMenuItem("zuxos_page_launcher", "桌面", PAGE_LAUNCHER),
                SettingsHostMenuItem("zuxos_page_package_installer", "软件包安装程序", PAGE_PACKAGE_INSTALLER),
                SettingsHostMenuItem("zuxos_page_general", "通用", PAGE_GENERAL),
            ),
        ),
        SettingsHostPage(
            id = PAGE_FRAMEWORK,
            title = "系统框架",
            summary = "设置系统框架功能",
            items = listOf(
                SettingsHostMenuItem("zuxos_page_core_patch", "核心破解", PAGE_CORE_PATCH),
                SettingsHostBooleanItem(ModuleConfig.DISABLE_FLAG_SECURE, "强制允许截屏", "解除截屏和录屏的限制"),
            ),
        ),
        SettingsHostPage(
            id = PAGE_CORE_PATCH,
            title = "核心破解",
            summary = "设置应用签名和安装检查",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.CORE_PATCH_ENABLED, "开启核心破解", "开启后需要重启手机才能生效"),
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
            id = PAGE_SYSTEM,
            title = "系统设置",
            summary = "设置系统增强功能",
            items = listOf(
                SettingsHostMenuItem("zuxos_page_app_info", "应用信息", PAGE_APP_INFO),
                SettingsHostMenuItem("zuxos_page_about_device", "关于设备", PAGE_ABOUT_DEVICE),
            ),
        ),
        SettingsHostPage(
            id = PAGE_APP_INFO,
            title = "应用信息设置",
            summary = "设置要显示哪些应用信息",
            items = listOf(
                SettingsHostBooleanItem("app_info_show_package_name", "显示包名", "在应用详情页显示当前应用的包名"),
                SettingsHostBooleanItem("app_info_show_sdk_version", "显示 SDK 版本", "显示应用的目标和最低 SDK 版本"),
                SettingsHostBooleanItem("app_info_show_install_time", "显示首次安装时间", "显示应用是什么时候装的"),
                SettingsHostBooleanItem("app_info_show_update_time", "显示最后更新时间", "显示应用上次更新的时间"),
                SettingsHostBooleanItem("app_info_show_install_source", "显示安装来源", "显示应用是从哪里装的"),
                SettingsHostBooleanItem("app_info_enable_long_copy", "启用长按复制", "长按就能复制应用信息"),
            ),
        ),
        SettingsHostPage(
            id = PAGE_ABOUT_DEVICE,
            title = "关于设备",
            summary = "修改联想设置里“关于平板电脑”页面的显示内容",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LENOVO_ABOUT_DEVICE_INFO_ENABLED, "启用关于设备自定义", "自定义“关于平板电脑”中的设备名称、型号、处理器、存储、内存、软件版本、Android 版本和图片"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_ENABLED, "替换设备图片", "使用选择的图片替换“关于平板电脑”顶部设备图"),
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
            id = PAGE_LENOVO_UPDATE,
            title = "系统更新",
            summary = "设置系统更新的提醒方式",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LENOVO_SYSTEM_UPDATE_HIDE_NEW_VERSION_REMINDER, "隐藏新版本提醒", "屏蔽更新小红点和通知，但保留系统更新入口"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_SYSTEM_UPDATE_FORCE_LOCAL_INSTALL, "强制显示本地安装入口", "一直显示本地更新选项，不影响原有功能"),
            ),
        ),
        SettingsHostPage(
            id = PAGE_GAME_ASSISTANT,
            title = "游戏助手",
            summary = "设置游戏助手的功能开关",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN, "使用拯救者系列游戏助手", "使用 Elden/拯救者系列游戏助手门禁，并修复 CPU 频率和温度显示"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT, "语音助手 / 变声器", "强制开启语音助手功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_BARRAGE, "弹幕", "强制开启弹幕功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT, "高光", "强制开启高光时刻功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_ISLAND, "游戏岛", "强制开启游戏岛功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION, "防误触", "强制开启防误触功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND, "通知免打扰", "强制开启免打扰功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD, "智能预下载", "强制开启智能预下载功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING, "智能旁路充电", "强制开启旁路充电功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE, "移动网络加速", "强制开启移动网络加速功能"),
                SettingsHostBooleanItem(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE, "主卡加速", "强制开启主卡加速功能"),
            ),
        ),
        SettingsHostPage(
            id = PAGE_SYSTEMUI,
            title = "系统界面",
            summary = "设置系统界面的显示效果",
            items = listOf(
                SettingsHostMenuItem("zuxos_page_control_center", "控制中心", PAGE_CONTROL_CENTER),
            ),
        ),
        SettingsHostPage(
            id = PAGE_CONTROL_CENTER,
            title = "控制中心",
            summary = "自定义控制中心快捷开关的颜色、透明度和背景模糊",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_ENABLED, "控制中心自定义总开关", "修改后需要重启系统界面才能生效"),
                SettingsHostColorItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_COLOR, "磁贴颜色(未开启)", "快捷开关未开启时的颜色", defaultValue = 0xFFFFFFFF.toInt()),
                SettingsHostColorItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_COLOR, "磁贴颜色(开启后)", "快捷开关开启后的颜色", defaultValue = 0xFF5FAE8B.toInt()),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_INACTIVE_ALPHA, "磁贴背景色透明度(未开启状态)", "未开启状态的快捷开关背景透明度", defaultValue = 0.35f, min = 0f, max = 1f),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_TILE_ACTIVE_ALPHA, "磁贴背景色透明度(开启后状态)", "已开启状态的快捷开关背景透明度", defaultValue = 0.92f, min = 0f, max = 1f),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PANEL_ALPHA, "控制中心整体透明度", "控制中心整体的透明度", defaultValue = 0.9f, min = 0f, max = 1f),
                SettingsHostFloatItem(ModuleConfig.LENOVO_SYSTEMUI_CONTROL_CENTER_PULL_DOWN_BLUR_RADIUS, "底层背景模糊强度", "下拉通知栏时背景的模糊程度", defaultValue = 18f, min = 0f, max = 200f),
            ),
        ),
        SettingsHostPage(
            id = PAGE_LAUNCHER,
            title = "桌面",
            summary = "设置 ZUX 桌面 Hook 功能",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_HOTSEAT_EXPAND, "更大的 Dock 栏", "把 Dock 图标数量上限提高到 20"),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_CUSTOM_GRID, "自定义桌面网格", "覆盖桌面行数和列数"),
                SettingsHostIntItem(ModuleConfig.LAUNCHER_CUSTOM_GRID_ROWS, "行数", "桌面网格行数", defaultValue = 4, min = 3, max = 12, visibleWhen = listOf(SettingsHostVisibility(ModuleConfig.LAUNCHER_CUSTOM_GRID))),
                SettingsHostIntItem(ModuleConfig.LAUNCHER_CUSTOM_GRID_COLUMNS, "列数", "桌面网格列数", defaultValue = 6, min = 3, max = 12, visibleWhen = listOf(SettingsHostVisibility(ModuleConfig.LAUNCHER_CUSTOM_GRID))),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_DISABLE_FORCE_STOP, "原生后台管理", "阻止最近任务清理强制停止受保护应用"),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_FORCE_STOP_WHITELIST_ENABLE, "白名单模式", "只保护白名单中的包名", visibleWhen = listOf(SettingsHostVisibility(ModuleConfig.LAUNCHER_DISABLE_FORCE_STOP))),
                SettingsHostTextItem(ModuleConfig.LAUNCHER_FORCE_STOP_WHITELIST, "受保护包名", "多个包名使用英文逗号分隔", visibleWhen = listOf(SettingsHostVisibility(ModuleConfig.LAUNCHER_DISABLE_FORCE_STOP), SettingsHostVisibility(ModuleConfig.LAUNCHER_FORCE_STOP_WHITELIST_ENABLE))),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_RECENTS_MEMORY, "最近任务显示内存", "在最近任务底部显示已用内存 / 总内存"),
                SettingsHostBooleanItem(ModuleConfig.LAUNCHER_HIDE_ICON_LABEL, "隐藏桌面图标名称", "隐藏桌面图标下方文字，不改变布局和点击区域"),
            ),
        ),
        SettingsHostPage(
            id = PAGE_PACKAGE_INSTALLER,
            title = "软件包安装程序",
            summary = "设置应用安装器的行为",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_DISABLE_SCAN, "跳过安全扫描", "跳过安全扫描，装应用更快"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_ALWAYS_ALLOW_PERMISSION, "总是给予应用权限", "装应用时自动授予所有权限"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_SKIP_WARN_PAGE, "跳过未上架警告页", "跳过联想的安全警告页面"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_DISABLE_INSTALLER_AD, "去广告", "安装完成后不显示推荐应用"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_STYLE_HOOK, "启用原生安装器", "用系统自带的安装界面代替 ZUI 的"),
                SettingsHostBooleanItem(ModuleConfig.PACKAGE_INSTALLER_DISABLE_DELETE, "不要删除安装包", "装完应用后保留安装包文件"),
            ),
        ),
        SettingsHostPage(
            id = PAGE_GENERAL,
            title = "通用",
            summary = "插件偏好和维护操作",
            items = listOf(
                SettingsHostBooleanItem(ModuleConfig.HOOK_LOGGING_ENABLED, "启用日志记录", "记录功能的注册、运行、失败等日志，开启后可以看到日志页面"),
                SettingsHostBooleanItem(ModuleConfig.SETTINGS_HOME_ENTRY_ENABLED, "在设置中显示入口", "在联想设置主页面显示 ZUXOS+ 入口", defaultValue = true),
            ),
        ),
    )

    fun page(id: String): SettingsHostPage? = pages.firstOrNull { it.id == id }
}
