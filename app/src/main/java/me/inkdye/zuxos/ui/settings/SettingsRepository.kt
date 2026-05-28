package me.inkdye.zuxos.ui.settings

import android.content.Context
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.ui.theme.ThemeMode
import org.json.JSONObject

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext

    fun getBoolean(spec: BooleanSettingSpec): Boolean =
        ModuleConfig.loadBoolean(appContext, spec.key, spec.defaultValue)

    fun setBoolean(spec: BooleanSettingSpec, value: Boolean) {
        ModuleConfig.saveBoolean(appContext, spec.key, value)
    }

    fun snapshot(): SettingsSnapshot {
        return SettingsSnapshot(
            themeMode = AppPreferences.getThemeMode(appContext),
            floatingNavEnabled = AppPreferences.isFloatingNavEnabled(appContext),
            liquidGlassEnabled = AppPreferences.isLiquidGlassEnabled(appContext),
            moduleBooleans = ModuleBooleanSettings.associate { spec ->
                spec.key to getBoolean(spec)
            },
        )
    }

    fun restore(snapshot: SettingsSnapshot) {
        AppPreferences.setThemeMode(appContext, snapshot.themeMode)
        AppPreferences.setFloatingNavEnabled(appContext, snapshot.floatingNavEnabled)
        AppPreferences.setLiquidGlassEnabled(appContext, snapshot.liquidGlassEnabled)
        ModuleBooleanSettings.forEach { spec ->
            setBoolean(spec, snapshot.moduleBooleans[spec.key] ?: spec.defaultValue)
        }
    }

    fun resetToDefaults() {
        AppPreferences.setThemeMode(appContext, ThemeMode.SYSTEM)
        AppPreferences.setFloatingNavEnabled(appContext, false)
        AppPreferences.setLiquidGlassEnabled(appContext, true)
        ModuleBooleanSettings.forEach { spec ->
            setBoolean(spec, spec.defaultValue)
        }
    }

    fun exportToJson(): String {
        val snapshot = snapshot()
        val root = JSONObject()
            .put("schema", EXPORT_SCHEMA)
            .put("themeMode", snapshot.themeMode.name)
            .put("floatingNavEnabled", snapshot.floatingNavEnabled)
            .put("liquidGlassEnabled", snapshot.liquidGlassEnabled)

        val module = JSONObject()
        snapshot.moduleBooleans.forEach { (key, value) ->
            module.put(key, value)
        }
        root.put("moduleBooleans", module)
        return root.toString(2)
    }

    fun importFromJson(json: String): SettingsSnapshot {
        val root = JSONObject(json)
        val module = root.optJSONObject("moduleBooleans") ?: JSONObject()
        val importedModuleBooleans = ModuleBooleanSettings.associate { spec ->
            spec.key to module.optBoolean(spec.key, spec.defaultValue)
        }

        val snapshot = SettingsSnapshot(
            themeMode = runCatching {
                ThemeMode.valueOf(root.optString("themeMode", ThemeMode.SYSTEM.name))
            }.getOrDefault(ThemeMode.SYSTEM),
            floatingNavEnabled = root.optBoolean("floatingNavEnabled", false),
            liquidGlassEnabled = root.optBoolean("liquidGlassEnabled", true),
            moduleBooleans = importedModuleBooleans,
        )
        restore(snapshot)
        return snapshot
    }

    companion object {
        const val EXPORT_SCHEMA = 1
    }
}

data class SettingsSnapshot(
    val themeMode: ThemeMode,
    val floatingNavEnabled: Boolean,
    val liquidGlassEnabled: Boolean,
    val moduleBooleans: Map<String, Boolean>,
)
