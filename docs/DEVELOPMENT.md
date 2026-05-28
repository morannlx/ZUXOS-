# ZUXOS+ Development Guide

This document is the maintainer-facing guide for ZUXOS+. It is written for
contributors, reviewers, and coding agents that need to understand the current
project before making changes.

## Project Overview

ZUXOS+ is an Android Xposed module for ZUXOS/ZUI-related system enhancements.
The module contains a Jetpack Compose settings app and several YukiHookAPI hook
groups for Android system behavior, Android Settings, Lenovo System Update, and
Package Installer.

The app UI is responsive across phone and tablet layouts. It includes feature
entry points, app/module settings, import/export, optional hook logs, root-aware
restart actions, theme preferences, floating navigation, and AndroidLiquidGlass
navigation effects.

## Tech Stack

- Kotlin 2.3.21, Java 17 target
- Android Gradle Plugin 8.10.1
- Gradle Wrapper 8.11.1
- Android SDK 36, minSdk 27
- Jetpack Compose with Material 3 and Compose BOM 2025.05.00
- YukiHookAPI 1.3.1 for Xposed integration
- KSP for YukiHookAPI and Hikage code generation
- BetterAndroid, Hikage, KavaRef, DrawableToolbox helper libraries
- AndroidLiquidGlass Backdrop (`io.github.kyant0:backdrop`) for Liquid Glass
- SharedPreferences and XSharedPreferences for module configuration

Key project values are defined in `gradle.properties`:

- `project.android.compileSdk=36`
- `project.android.targetSdk=36`
- `project.android.minSdk=27`
- `project.app.packageName=me.inkdye.zuxos`
- `project.app.versionName="1.0.0"`
- `project.app.versionCode=1`

Dependency versions are centralized in `gradle/libs.versions.toml`. Repository
resolution is configured in `settings.gradle.kts` with Google Maven, Maven
Central, Xposed Maven, and JitPack.

## Directory Structure

```text
.
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   └── xposed_init
│       ├── java/me/inkdye/zuxos/
│       │   ├── application/
│       │   ├── config/
│       │   ├── corepatch/
│       │   ├── hook/
│       │   │   ├── lenovo/
│       │   │   ├── packageinstaller/
│       │   │   ├── settings/
│       │   │   └── systemframework/
│       │   ├── logging/
│       │   ├── root/
│       │   └── ui/
│       │       ├── activity/
│       │       ├── components/
│       │       ├── root/
│       │       ├── settings/
│       │       └── theme/
│       └── res/
│           ├── values/
│           ├── values-zh/
│           ├── values-zh-rCN/
│           └── xml/
├── docs/
│   └── DEVELOPMENT.md
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

Important packages:

- `application`: app startup and default setting initialization.
- `config`: cross-process module configuration API.
- `corepatch`: Android system Core Patch implementations for API 28+.
- `hook`: Xposed entry point, hook registry, logging bridge, and hook modules.
- `logging`: `ContentProvider` bridge and local JSONL hook event storage.
- `root`: root availability checks and restart commands for target scopes.
- `ui/activity`: Compose-backed screens and settings pages.
- `ui/components`: reusable Compose UI components.
- `ui/settings`: setting definitions, repository, state wrappers, and app prefs.
- `ui/theme`: light/dark color schemes and theme mode helpers.

## Environment Requirements

- Android Studio compatible with AGP 8.10.x.
- JDK 17.
- Android SDK Platform 36 installed.
- A device or emulator running Android 8.1+ for the app shell.
- LSPosed or another compatible Xposed environment for hook testing.
- Target apps available for the hook group being tested:
  `android`, `com.android.settings`, `com.lenovo.ota`,
  `com.android.packageinstaller`, or `com.google.android.packageinstaller`.
- Network access for first-time Gradle dependency resolution.

## Install And Start

1. Clone the repository.

   ```bash
   git clone <repo-url>
   cd ZUXOS+
   ```

2. Open the project in Android Studio or build from the terminal.

   ```bash
   ./gradlew :app:assembleDebug
   ```

3. Install the debug APK.

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. Enable the module in LSPosed and select the scopes required by the feature
   being tested. Current scopes are declared in `res/values/array.xml`:

   ```text
   me.inkdye.zuxos
   android
   com.android.settings
   com.lenovo.ota
   com.android.packageinstaller
   com.google.android.packageinstaller
   ```

5. Reboot or force-stop/restart the affected target app. The settings app can
   request root restart actions for supported scopes when root is available.

6. Launch `ZUXOS+` from the launcher or from LSPosed module settings.

## Core Features

### Compose Settings App

`MainActivity` shows the splash screen, starts `LiquidGlassHomeActivity`, and
finishes. `LiquidGlassHomeActivity` hosts the main Compose app with these tabs:

- Home: activation state, module info, root state, restart cards, and device
  information.
- Features: entry points for framework, Android Settings, Lenovo System Update,
  and Package Installer feature pages.
- Logs: visible only when hook logging is enabled.
- Settings: general settings, appearance settings, import/export, and about.

Phone layouts use bottom navigation. Tablet layouts use a left navigation rail.
Navigation can be fixed or floating, and Liquid Glass can be enabled or disabled
from appearance settings.

### Feature Groups

Current hook feature groups are:

- Core Patch: framework-level package/signature behavior for Android Q through
  Android V/API 35+ paths.
- System Framework: disables secure window checks through
  `DisableFlagSecure`.
- App Info: injects optional metadata into Android Settings app-info headers.
- Lenovo System Update: hides new-version reminders and forces local install
  menu availability.
- Package Installer: disables scan flows, always allows the trust permission
  option, skips warning pages, removes installer recommendations, applies a
  style override, and disables delete behavior.

### Hook Entry And Registration

`HookEntry` is annotated with `@InjectYukiHookWithXposed`. Its initializer
registers hook objects through `Hooks.register(...)`, and `onHook()` filters
them by `targetPackages`.

Hook installation is gated by `ModuleConfig.isEnabled(prefKey, defaultEnabled)`
unless a hook sets `alwaysRegister = true`. Core Patch is handled separately for
the `android` package because it chooses an implementation based on
`Build.VERSION.SDK_INT`.

### Module Settings Repository

Xposed-facing settings are defined in `SettingsDefinitions.kt` and persisted via
`SettingsRepository`. UI state is wrapped by `SettingsState.kt`.

Use this path for new hook settings:

1. Add a key to `SettingKeys` or `ModuleConfig`.
2. Add a `BooleanSettingSpec` or another typed setting spec.
3. Include the spec in the relevant feature list and in
   `ModuleBooleanSettings` if it should import/export or reset with module
   defaults.
4. Render it with a reusable component such as `BooleanSettingSwitch`.
5. Read it from hook code through `ModuleConfig.isEnabled(...)`.

### Configuration Storage

There are two configuration paths:

- `AppPreferences`: app-only preferences such as theme and navigation mode.
- `ModuleConfig`: Xposed-facing configuration stored in
  `xposed_module_config`, readable from hook processes through
  `XSharedPreferences`.

`ModuleConfig` writes with `MODE_WORLD_READABLE` through a package context so
hook processes can read preferences. Treat changes to this path carefully and
test with LSPosed before merging.

### Hook Logs

When `HookLoggingSetting` is enabled, hook processes send events through
`HookLogProvider` to the app process. Events are persisted by `HookLogStore` as
JSONL under the app files directory, capped and trimmed automatically.

`HookLogsPage` reads the local store, supports filtering by hook name, module
name, and target package, and can refresh or clear the log.

### Root Restart Actions

`RootManager` checks for `su`, requests root access, and can restart target
scopes with predefined commands:

- Settings: force-stops `com.android.settings`.
- Package Installer: force-stops AOSP and Google package installer packages.
- Lenovo System Update: force-stops OTA, Settings, and ZUI launcher.
- Android System: kills `system_server`.

Root actions are optional conveniences for testing. Hook functionality should
not depend on root access.

### Import And Export

`ExportImportSettingsActivity` uses Android's document picker APIs:

- Export writes a JSON snapshot with schema `SettingsRepository.EXPORT_SCHEMA`.
- Import reads JSON and restores app preferences plus module booleans.

No broad storage permission is required. Keep the JSON schema backward
compatible; if the shape changes, increment `EXPORT_SCHEMA` and keep old-schema
reads.

## Development Flow

1. Inspect the existing pattern before editing. Most new UI should reuse
   `AppBackground`, `PageHeader`, `GlassCard`, `GlassSwitch`, `ActionCard`, or
   `BooleanSettingSwitch`.
2. Keep user-facing strings in `res/values/strings.xml` and update Chinese
   resources in `values-zh` and `values-zh-rCN` when adding new text.
3. Keep configuration keys centralized in `SettingsDefinitions.kt` or
   `ModuleConfig`.
4. Keep persistence logic in `SettingsRepository`, `AppPreferences`, or
   `ModuleConfig`; avoid direct SharedPreferences calls from screens.
5. Register new Activity classes in `AndroidManifest.xml`.
6. Register new hooks in the `HookEntry` initializer through
   `Hooks.register(...)`.
7. Update `res/values/array.xml` if a hook requires a new LSPosed scope.
8. Build with `./gradlew :app:assembleDebug` before submitting changes.

## Common Commands

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Build release APK
./gradlew :app:assembleRelease

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run Android instrumentation tests
./gradlew :app:connectedDebugAndroidTest

# Clean generated build output
./gradlew clean
```

## Adding A New Hook

1. Create a hook object implementing `Hook`.

   ```kotlin
   object MyHook : Hook {
       override val prefKey = "my_hook_enabled"
       override val defaultEnabled = false
       override val targetPackage = "target.package.name"

       override fun PackageParam.onHook() {
           loadApp(targetPackage!!) {
               // Hook implementation
           }
       }
   }
   ```

2. Add a setting definition in `SettingsDefinitions.kt`.
3. Add UI to the relevant settings page.
4. Register the hook in the `HookEntry` initializer.
5. Add or update LSPosed scopes in `res/values/array.xml` if needed.
6. Test with LSPosed and the target app in scope.

Use `targetPackages` instead of only `targetPackage` when one hook supports
multiple package names. Package Installer hooks use this for AOSP and Google
installer packages.

## Adding A New Settings Page

1. Create an Activity in `ui/activity`.
2. Use `enableEdgeToEdge()` and `setContent`.
3. Wrap content in `AppBackground` unless the page intentionally owns its full
   background.
4. Use `PageHeader(..., onBack = { finish() })`.
5. Read theme with `rememberAppSettings()`.
6. Add the Activity to `AndroidManifest.xml`.
7. Add the launcher card or route from an existing page.

## UI Guidelines

- Prefer semantic colors from `AppColorScheme`.
- Avoid hard-coded user-facing text in Compose code.
- Keep cards and switches reusable.
- Keep page spacing consistent: horizontal padding is usually `20.dp`, top
  content padding is usually `24.dp`, and bottom padding is usually `100.dp` or
  more when navigation overlays content.
- Liquid Glass effects should gracefully fall back to opaque surfaces when the
  Liquid Glass preference is disabled.
- Preserve responsive behavior for phone, compact tablet, and wider tablet
  widths.

## Known Warnings

Debug builds can emit warnings from generated YukiHookAPI/KSP code about
invisible references and future Kotlin behavior. They are generated-code
warnings and do not currently block debug builds.

`ModuleConfig` intentionally uses `MODE_WORLD_READABLE` for Xposed
cross-process preferences. Do not replace it without validating hook-process
reads on LSPosed.

## Maintenance Recommendations

- Keep `gradle/libs.versions.toml` as the single dependency version source.
- Update AGP, Kotlin, Compose, and YukiHookAPI together only after a full debug
  build and LSPosed smoke test.
- Add new hook settings through `SettingsDefinitions.kt` or `ModuleConfig`
  instead of scattering raw keys in UI code.
- Keep import/export schema backward compatible.
- Prefer small hook implementations with clear `prefKey` ownership.
- When changing Android Settings or Package Installer hooks, test on multiple
  Android versions and ROMs because internal classes vary by vendor and API
  level.
- When changing Core Patch behavior, test each supported API family separately:
  Q, R, S, T, U, and V/API 35+.
