# ZUXOS+

[![Xposed Module](https://img.shields.io/badge/Xposed-Module-brightgreen.svg)](https://github.com/morannlx/ZUXOS+)
[![YukiHookAPI](https://img.shields.io/badge/Framework-YukiHookAPI-blue.svg)](https://github.com/HighCapable/YukiHookAPI)

**ZUXOS+** is an Android Xposed module for ZUXOS/ZUI-related system
enhancements. It provides a Compose settings app, configurable hook groups,
hook event logs, import/export support, and LSPosed-friendly module scopes.

## Features

- System framework: Core Patch options and `FLAG_SECURE` bypass.
- Android Settings: app-info metadata extension.
- Lenovo System Update: hide new-version reminders and expose local install.
- Package Installer: scan bypass, permission trust flow, warning skip, ad
  removal, style override, and delete-button behavior.
- Hook logs: optional JSONL event recording with in-app filtering and clearing.
- Settings app: responsive phone/tablet UI with theme, floating navigation, and
  Liquid Glass navigation effects.

## Project Structure

```text
app/src/main/java/me/inkdye/zuxos/
├── application/        # App startup and defaults
├── config/             # Cross-process module configuration
├── corepatch/          # Android system Core Patch implementations
├── hook/               # Hook interface, registry, entry point, and modules
├── logging/            # Hook event provider and local log store
├── root/               # Root status and restart commands
└── ui/                 # Compose activities, components, settings, and theme
```

## Build

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Tech Stack

- Kotlin 2.3.21, Java 17 target
- Android Gradle Plugin 8.10.1, Gradle Wrapper 8.11.1
- Android SDK 36, minSdk 27
- Jetpack Compose with Material 3, Compose BOM 2025.05.00
- YukiHookAPI 1.3.1 with KSP
- BetterAndroid, Hikage, KavaRef, AndroidLiquidGlass Backdrop

See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for the full maintainer guide.
