# ZUXOS+

[![Xposed Module](https://img.shields.io/badge/Xposed-Module-brightgreen.svg)](https://github.com/morannlx/ZUXOS+)
[![YukiHookAPI](https://img.shields.io/badge/Framework-YukiHookAPI-blue.svg)](https://github.com/HighCapable/YukiHookAPI)

**ZUXOS+** is an Xposed module development template built with YukiHookAPI.

## Project Structure

```
app/src/main/java/me/inkdye/zuxos/
├── application/
│   └── DefaultApplication.kt    # Module Application
├── hook/
│   ├── Hook.kt                  # Hook interface
│   ├── Hooks.kt                 # Hook registry
│   └── HookEntry.kt             # Xposed entry point
└── ui/activity/
    └── MainActivity.kt          # Module settings UI
```

## Adding a New Hook

1. Create a new class implementing `Hook` interface in `hook/`
2. Register it in `HookEntry.kt` via `Hooks.register(...)`
3. The framework handles pref checking and `loadApp` automatically

## Build

```bash
./gradlew assembleDebug
```

## Tech Stack

- Kotlin + YukiHookAPI 1.3.1
- Material Design 3 (DynamicColors)
- Android SDK 36 (minSdk 27)
