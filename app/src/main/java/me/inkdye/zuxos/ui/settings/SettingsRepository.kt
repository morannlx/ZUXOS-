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

    fun getFloat(spec: FloatSettingSpec): Float =
        ModuleConfig.loadFloat(appContext, spec.key, spec.defaultValue)

    fun setFloat(spec: FloatSettingSpec, value: Float) {
        ModuleConfig.saveFloat(appContext, spec.key, value.coerceIn(spec.valueRange))
    }

    fun getColor(spec: ColorSettingSpec): Int =
        ModuleConfig.loadInt(appContext, spec.key, spec.defaultValue)

    fun setColor(spec: ColorSettingSpec, value: Int) {
        ModuleConfig.saveInt(appContext, spec.key, value)
    }

    fun snapshot(): SettingsSnapshot {
        return SettingsSnapshot(
            themeMode = AppPreferences.getThemeMode(appContext),
            floatingNavEnabled = AppPreferences.isFloatingNavEnabled(appContext),
            liquidGlassEnabled = AppPreferences.isLiquidGlassEnabled(appContext),
            moduleBooleans = ModuleBooleanSettings.associate { spec ->
                spec.key to getBoolean(spec)
            },
            moduleFloats = ModuleFloatSettings.associate { spec ->
                spec.key to getFloat(spec)
            },
            moduleColors = ModuleColorSettings.associate { spec ->
                spec.key to getColor(spec)
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
        ModuleFloatSettings.forEach { spec ->
            setFloat(spec, snapshot.moduleFloats[spec.key] ?: spec.defaultValue)
        }
        ModuleColorSettings.forEach { spec ->
            setColor(spec, snapshot.moduleColors[spec.key] ?: spec.defaultValue)
        }
    }

    fun resetToDefaults() {
        AppPreferences.setThemeMode(appContext, ThemeMode.SYSTEM)
        AppPreferences.setFloatingNavEnabled(appContext, false)
        AppPreferences.setLiquidGlassEnabled(appContext, true)
        ModuleBooleanSettings.forEach { spec ->
            setBoolean(spec, spec.defaultValue)
        }
        ModuleFloatSettings.forEach { spec ->
            setFloat(spec, spec.defaultValue)
        }
        ModuleColorSettings.forEach { spec ->
            setColor(spec, spec.defaultValue)
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
        val floats = JSONObject()
        snapshot.moduleFloats.forEach { (key, value) ->
            floats.put(key, value.toDouble())
        }
        root.put("moduleFloats", floats)
        val colors = JSONObject()
        snapshot.moduleColors.forEach { (key, value) ->
            colors.put(key, value)
        }
        root.put("moduleColors", colors)
        return root.toString(2)
    }

    fun importFromJson(json: String): SettingsSnapshot {
        val root = JSONObject(json)
        val module = root.optJSONObject("moduleBooleans") ?: JSONObject()
        val floats = root.optJSONObject("moduleFloats") ?: JSONObject()
        val colors = root.optJSONObject("moduleColors") ?: JSONObject()
        val importedModuleBooleans = ModuleBooleanSettings.associate { spec ->
            spec.key to module.optBoolean(spec.key, spec.defaultValue)
        }
        val importedModuleFloats = ModuleFloatSettings.associate { spec ->
            spec.key to floats.optDouble(spec.key, spec.defaultValue.toDouble()).toFloat()
        }
        val importedModuleColors = ModuleColorSettings.associate { spec ->
            spec.key to colors.optInt(spec.key, spec.defaultValue)
        }

        val snapshot = SettingsSnapshot(
            themeMode = runCatching {
                ThemeMode.valueOf(root.optString("themeMode", ThemeMode.SYSTEM.name))
            }.getOrDefault(ThemeMode.SYSTEM),
            floatingNavEnabled = root.optBoolean("floatingNavEnabled", false),
            liquidGlassEnabled = root.optBoolean("liquidGlassEnabled", true),
            moduleBooleans = importedModuleBooleans,
            moduleFloats = importedModuleFloats,
            moduleColors = importedModuleColors,
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
    val moduleFloats: Map<String, Float>,
    val moduleColors: Map<String, Int>,
)
