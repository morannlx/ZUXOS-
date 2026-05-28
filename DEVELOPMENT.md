# ZUXOS+ Development Guide

This document is the maintainer-facing guide for ZUXOS+. It is written for
contributors, reviewers, and AI coding agents that need to understand the
project quickly before making changes.

## Project Overview

ZUXOS+ is an Android Xposed module for ZUXOS-related system enhancements. The
current module provides a Compose-based settings application and a YukiHookAPI
hook that extends Android Settings app-info pages with configurable metadata.

The app UI is a Jetpack Compose experience with responsive phone/tablet
navigation, theme preferences, import/export support, and AndroidLiquidGlass
visual effects for selected navigation surfaces.

## Tech Stack

- Kotlin, Java 17 target
- Android Gradle Plugin 8.10.1
- Gradle Wrapper 8.11.1
- Jetpack Compose with Material 3
- YukiHookAPI for Xposed integration
- KSP for hook code generation
- BetterAndroid and Hikage helper libraries
- AndroidLiquidGlass Backdrop (`io.github.kyant0:backdrop`) for Liquid Glass
- SharedPreferences and XSharedPreferences for module configuration

Key SDK values are defined in `gradle.properties`:

- `compileSdk=36`
- `targetSdk=36`
- `minSdk=27`
- `applicationId=me.inkdye.zuxos`
- `versionName=1.0.0`
- `versionCode=1`

## Directory Structure

```text
.
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/me/inkdye/zuxos/
│       │   ├── application/
│       │   │   └── DefaultApplication.kt
│       │   ├── config/
│       │   │   └── ModuleConfig.kt
│       │   ├── hook/
│       │   │   ├── Hook.kt
│       │   │   ├── HookEntry.kt
│       │   │   ├── Hooks.kt
│       │   │   └── settings/
│       │   │       └── AppInfoExtend.kt
│       │   └── ui/
│       │       ├── activity/
│       │       ├── components/
│       │       ├── settings/
│       │       └── theme/
│       └── res/
│           ├── values/
│           ├── values-zh/
│           ├── values-zh-rCN/
│           └── xml/
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

Important packages:

- `application`: app startup, dynamic colors, and default config initialization.
- `config`: cross-process module configuration API.
- `hook`: Xposed entry point, hook registry, and hook implementations.
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
- Network access for Gradle dependency resolution from Google Maven, Maven
  Central, Xposed Maven, and JitPack.

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

4. For Xposed behavior, enable the module in LSPosed, select the target scope
   (`com.android.settings`), then reboot or force-stop/restart the target app.

5. Launch `ZUXOS+` from the app launcher or from the LSPosed module settings
   entry.

## Core Features

### Compose Settings App

`LiquidGlassHomeActivity` hosts the main app with three tabs:

- Home: activation state, module info, and device info.
- Features: entry points for supported hook feature groups.
- Settings: general settings, appearance settings, import/export, and about.

Phone layouts use a bottom navigation surface. Tablet layouts use a left rail.
The navigation supports fixed and floating modes.

### Appearance System

Appearance preferences are stored through `AppPreferences`:

- Theme mode: light, dark, or system.
- Floating navigation.
- Liquid Glass navigation effects.

Theme colors come from `AppColorScheme` and are resolved with
`colorSchemeFor(resolveIsDark(...))`.

### Module Settings Repository

Xposed-facing settings are defined in `SettingsDefinitions.kt` and persisted via
`SettingsRepository`. UI state is wrapped by `SettingsState.kt`.

Use this path for new hook settings:

1. Add a key to `SettingKeys`.
2. Add a `BooleanSettingSpec` or a new typed setting spec.
3. Render it with a reusable component such as `BooleanSettingSwitch`.
4. Read it from hook code via `ModuleConfig.isEnabled(...)`.

### App Info Hook

`AppInfoExtend` injects configured metadata into Android Settings app-info
headers. It can show package name, SDK versions, install/update time, installer
package, and optional long-press copy behavior.

The hook is registered in `HookEntry` through `Hooks.register(...)`. Hook loading
is gated by the same `ModuleConfig` keys that the UI writes.

### Import And Export

`ExportImportSettingsActivity` uses Android's document picker APIs:

- Export writes a JSON snapshot.
- Import reads JSON and restores app preferences plus module booleans.

No broad storage permission is required.

## Development Flow

1. Inspect the existing pattern before editing. Most new UI should reuse
   `AppBackground`, `PageHeader`, `GlassCard`, `GlassSwitch`, `ActionCard`, or
   `BooleanSettingSwitch`.
2. Keep user-facing strings in `res/values/strings.xml` and update Chinese
   resources when adding new text.
3. Keep configuration keys centralized in `SettingsDefinitions.kt`.
4. Keep persistence logic in `SettingsRepository`, `AppPreferences`, or
   `ModuleConfig`; avoid direct SharedPreferences calls from screens.
5. Register new Activity classes in `AndroidManifest.xml`.
6. Register new hooks in `HookEntry`.
7. Build with `./gradlew :app:assembleDebug` before submitting changes.

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

## Configuration Notes

There are two configuration paths:

- `AppPreferences`: app-only preferences such as theme and navigation mode.
- `ModuleConfig`: Xposed-facing configuration stored in
  `xposed_module_config`, readable from hook processes through
  `XSharedPreferences`.

`DefaultApplication` initializes default values on app startup. If a setting must
affect hook loading, it must be represented in `ModuleConfig` and should have a
default in `SettingsDefinitions.kt`.

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
4. Register the hook in `HookEntry`.
5. Test with LSPosed and the target app in scope.

## Adding A New Settings Page

1. Create an Activity in `ui/activity`.
2. Use `enableEdgeToEdge()` and `setContent`.
3. Wrap content in `AppBackground`.
4. Use `PageHeader(..., onBack = { finish() })`.
5. Read theme with `rememberAppSettings()`.
6. Add the Activity to `AndroidManifest.xml`.
7. Add the launcher card or route from an existing page.

## UI Guidelines

- Prefer semantic colors from `AppColorScheme`.
- Avoid hard-coded user-facing text in Compose code.
- Keep cards and switches reusable.
- Keep page spacing consistent: horizontal padding is usually `20.dp`, top
  content padding is usually `24.dp`, bottom padding is usually `100.dp` or more
  when navigation overlays content.
- Liquid Glass effects should gracefully fall back to opaque surfaces when the
  Liquid Glass preference is disabled.

## Known Warnings

Debug builds currently emit warnings from generated YukiHookAPI/KSP code about
invisible references and future Kotlin behavior. They are generated-code
warnings and do not currently block debug builds.

`ModuleConfig` uses `MODE_WORLD_READABLE` intentionally for Xposed
cross-process preferences. Treat changes to this path carefully and test with
LSPosed before merging.

## Maintenance Recommendations

- Keep `gradle/libs.versions.toml` as the single dependency version source.
- Update AGP, Kotlin, Compose, and YukiHookAPI together only after a full debug
  build and LSPosed smoke test.
- Add new hook settings through `SettingsDefinitions.kt` instead of scattering
  raw keys in UI code.
- Keep import/export schema backward compatible. If the JSON shape changes,
  increment `SettingsRepository.EXPORT_SCHEMA` and keep old-schema reads.
- Prefer small hook implementations with clear `prefKey` ownership.
- When changing Android Settings hooks, test on multiple Android versions
  because Settings internals vary by ROM and API level.
