# ZUXOS+ 开发指南

## 项目概览

ZUXOS+ 是一个基于 Xposed 框架的 Android 系统增强模块，使用 Kotlin + Jetpack Compose 构建 UI，集成 Liquid Glass 液态玻璃视觉效果。

## 目录结构

```
app/src/main/java/me/inkdye/zuxos/
├── application/          # Application 入口
│   └── DefaultApplication.kt
├── config/               # 模块配置（跨进程 SharedPreferences）
│   └── ModuleConfig.kt
├── hook/                 # Xposed Hook 逻辑
│   ├── HookEntry.kt      # Hook 入口
│   ├── Hook.kt           # 单个 Hook 实现
│   ├── Hooks.kt          # Hook 注册表
│   └── settings/         # 系统设置相关 Hook
│       └── AppInfoExtend.kt
├── ui/                   # UI 层
│   ├── activity/         # Activity 页面
│   │   ├── MainActivity.kt
│   │   ├── LiquidGlassHomeActivity.kt   # 主页面（首页/功能/设置）
│   │   ├── SystemSettingsActivity.kt    # 系统设置页
│   │   └── AppInfoSettingsActivity.kt   # 应用信息设置页
│   ├── components/       # 可复用 Compose 组件
│   │   ├── AppBackground.kt     # 统一背景
│   │   ├── GlassCard.kt         # 毛玻璃卡片
│   │   ├── GlassSwitch.kt       # 开关组件
│   │   ├── InfoRow.kt           # 信息行
│   │   ├── PageHeader.kt        # 页面标题
│   │   └── ThemeModeSelector.kt # 主题选择器
│   ├── settings/         # 应用偏好设置
│   │   └── AppPreferences.kt    # 持久化存储管理
│   └── theme/            # 主题系统
│       ├── AppColorScheme.kt    # 颜色方案（Light/Dark）
│       ├── ThemeMode.kt         # 主题模式枚举
│       └── ThemeUtils.kt        # 主题工具函数
```

## 核心架构

### 1. 主题系统

```
ThemeMode (enum)
  ├── LIGHT    → 强制浅色
  ├── DARK     → 强制深色
  └── SYSTEM   → 跟随系统
       ↓
resolveIsDark(mode) → Boolean
       ↓
colorSchemeFor(isDark) → AppColorScheme
       ↓
所有组件通过 colorScheme 参数获取颜色
```

**使用方式：**
```kotlin
@Composable
fun MyPage() {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))

    Text("Hello", color = cs.textPrimary)
}
```

### 2. 设置持久化

使用 `AppPreferences` 管理用户偏好，底层为 `SharedPreferences`：

```kotlin
// 读取
AppPreferences.isFloatingNavEnabled(context)

// 写入
AppPreferences.setFloatingNavEnabled(context, true)
```

在 Compose 中通过 `AppSettingsState` 响应式读写：

```kotlin
val settings = rememberAppSettings()

// 读取（可观察）
settings.floatingNavEnabled.value

// 写入（自动持久化 + 触发重组）
settings.updateFloatingNav(true)
```

### 3. Liquid Glass 效果

依赖 [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) 库：

```kotlin
val backdrop = rememberLayerBackdrop {
    drawRect(backgroundBrush)
    drawContent()
}

// 绑定 backdrop 到内容区域
Modifier.layerBackdrop(backdrop)

// 在目标组件上绘制效果
Modifier.drawBackdrop(
    backdrop = backdrop,
    shape = { RoundedCornerShape(24.dp) },
    effects = {
        vibrancy()
        blur(8f.dp.toPx())
        lens(12f.dp.toPx(), 20f.dp.toPx())
    },
    onDrawSurface = {
        drawRect(glassColor.copy(alpha = 0.30f))
    }
)
```

效果开关通过 `settings.liquidGlassEnabled.value` 控制，关闭时降级为半透明渐变背景。

## 开发规范

### 颜色使用

- **禁止硬编码颜色值**，统一使用 `AppColorScheme` 中的语义化颜色
- 新增颜色时在 `AppColorScheme` 中同时定义 Light/Dark 两套值
- 使用 `cs.textPrimary` 而非 `Color.Black`，使用 `cs.cardBackground` 而非 `Color.White`

### 组件复用

- 通用组件放入 `ui/components/` 目录
- 组件接受 `colorScheme: AppColorScheme` 参数以支持主题
- 避免在组件内部调用 `isSystemInDarkTheme()`，由调用方传入颜色方案

### 状态管理

- UI 状态使用 Compose `State` / `MutableState`
- 持久化偏好使用 `AppPreferences` + `AppSettingsState`
- Xposed Hook 配置使用 `ModuleConfig`（跨进程）

### 新增页面

1. 在 `ui/activity/` 创建 Activity
2. 在 `AndroidManifest.xml` 注册
3. 使用 `AppColorScheme` 统一颜色
4. 使用公共组件（GlassCard、PageHeader 等）

### 新增设置项

1. 在 `values/strings.xml` 和 `values-zh/strings.xml` 添加字符串
2. 在 `AppPreferences` 添加 key 和读写方法
3. 在 `AppSettingsState` 添加响应式状态
4. 在设置页面 `SettingsPage` 添加 UI

## 依赖说明

| 依赖 | 用途 |
|------|------|
| `backdrop` (io.github.kyant0) | Liquid Glass 液态玻璃效果 |
| `yukihookapi` | Xposed Hook 框架 |
| `compose-bom` | Jetpack Compose BOM |
| `material3` | Material 3 组件 |
| `betterandroid` | Android 工具库 |
| `hikage` | Compose 编译器插件 |

## 构建与运行

```bash
# Debug 构建
./gradlew :app:assembleDebug

# Release 构建
./gradlew :app:assembleRelease
```

需要在 LSPosed 管理器中激活模块并重启设备。
