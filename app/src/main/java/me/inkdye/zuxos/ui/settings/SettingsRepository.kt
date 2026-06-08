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

    fun getText(spec: TextSettingSpec): String =
        ModuleConfig.loadString(appContext, spec.key, spec.defaultValue)

    fun setText(spec: TextSettingSpec, value: String) {
        ModuleConfig.saveString(appContext, spec.key, value)
    }

    fun getInt(spec: IntSettingSpec): Int =
        ModuleConfig.loadInt(appContext, spec.key, spec.defaultValue).coerceIn(spec.valueRange)

    fun setInt(spec: IntSettingSpec, value: Int) {
        ModuleConfig.saveInt(appContext, spec.key, value.coerceIn(spec.valueRange))
    }

    fun getString(key: String, defaultValue: String = ""): String =
        ModuleConfig.loadString(appContext, key, defaultValue)

    fun setString(key: String, value: String) {
        ModuleConfig.saveString(appContext, key, value)
    }

    fun snapshot(): SettingsSnapshot {
        return SettingsSnapshot(
            themeMode = AppPreferences.getThemeMode(appContext),
            floatingNavEnabled = AppPreferences.isFloatingNavEnabled(appContext),
            liquidGlassEnabled = AppPreferences.isLiquidGlassEnabled(appContext),
            glassCardOpacity = AppPreferences.getGlassCardOpacity(appContext),
            titleTextContrast = AppPreferences.getTitleTextContrast(appContext),
            descriptionTextContrast = AppPreferences.getDescriptionTextContrast(appContext),
            moduleBooleans = ModuleBooleanSettings.associate { spec ->
                spec.key to getBoolean(spec)
            },
            moduleFloats = ModuleFloatSettings.associate { spec ->
                spec.key to getFloat(spec)
            },
            moduleColors = ModuleColorSettings.associate { spec ->
                spec.key to getColor(spec)
            },
            moduleTexts = ModuleTextSettings.associate { spec ->
                spec.key to getText(spec)
            } + (ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH to getString(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH)),
            moduleInts = ModuleIntSettings.associate { spec ->
                spec.key to getInt(spec)
            },
        )
    }

    fun restore(snapshot: SettingsSnapshot) {
        AppPreferences.setThemeMode(appContext, snapshot.themeMode)
        AppPreferences.setFloatingNavEnabled(appContext, snapshot.floatingNavEnabled)
        AppPreferences.setLiquidGlassEnabled(appContext, snapshot.liquidGlassEnabled)
        AppPreferences.setGlassCardOpacity(appContext, snapshot.glassCardOpacity)
        AppPreferences.setTitleTextContrast(appContext, snapshot.titleTextContrast)
        AppPreferences.setDescriptionTextContrast(appContext, snapshot.descriptionTextContrast)
        ModuleBooleanSettings.forEach { spec ->
            setBoolean(spec, snapshot.moduleBooleans[spec.key] ?: spec.defaultValue)
        }
        ModuleFloatSettings.forEach { spec ->
            setFloat(spec, snapshot.moduleFloats[spec.key] ?: spec.defaultValue)
        }
        ModuleColorSettings.forEach { spec ->
            setColor(spec, snapshot.moduleColors[spec.key] ?: spec.defaultValue)
        }
        ModuleTextSettings.forEach { spec ->
            setText(spec, snapshot.moduleTexts[spec.key] ?: spec.defaultValue)
        }
        ModuleIntSettings.forEach { spec ->
            setInt(spec, snapshot.moduleInts[spec.key] ?: spec.defaultValue)
        }
        snapshot.moduleTexts[ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH]?.let { path ->
            setString(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH, path)
        }
    }

    fun resetToDefaults() {
        AppPreferences.setThemeMode(appContext, ThemeMode.SYSTEM)
        AppPreferences.setFloatingNavEnabled(appContext, false)
        AppPreferences.setLiquidGlassEnabled(appContext, true)
        AppPreferences.setGlassCardOpacity(appContext, AppPreferences.DEFAULT_GLASS_CARD_OPACITY)
        AppPreferences.setTitleTextContrast(appContext, AppPreferences.DEFAULT_TEXT_CONTRAST)
        AppPreferences.setDescriptionTextContrast(appContext, AppPreferences.DEFAULT_TEXT_CONTRAST)
        ModuleBooleanSettings.forEach { spec ->
            setBoolean(spec, spec.defaultValue)
        }
        ModuleFloatSettings.forEach { spec ->
            setFloat(spec, spec.defaultValue)
        }
        ModuleColorSettings.forEach { spec ->
            setColor(spec, spec.defaultValue)
        }
        ModuleTextSettings.forEach { spec ->
            setText(spec, spec.defaultValue)
        }
        ModuleIntSettings.forEach { spec ->
            setInt(spec, spec.defaultValue)
        }
        setString(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH, "")
    }

    fun exportToJson(): String {
        val snapshot = snapshot()
        val root = JSONObject()
            .put("schema", EXPORT_SCHEMA)
            .put("themeMode", snapshot.themeMode.name)
            .put("floatingNavEnabled", snapshot.floatingNavEnabled)
            .put("liquidGlassEnabled", snapshot.liquidGlassEnabled)
            .put("glassCardOpacity", snapshot.glassCardOpacity.toDouble())
            .put("titleTextContrast", snapshot.titleTextContrast.toDouble())
            .put("descriptionTextContrast", snapshot.descriptionTextContrast.toDouble())

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
        val texts = JSONObject()
        snapshot.moduleTexts.forEach { (key, value) ->
            texts.put(key, value)
        }
        root.put("moduleTexts", texts)
        val ints = JSONObject()
        snapshot.moduleInts.forEach { (key, value) ->
            ints.put(key, value)
        }
        root.put("moduleInts", ints)
        return root.toString(2)
    }

    fun importFromJson(json: String): SettingsSnapshot {
        val root = JSONObject(json)
        val module = root.optJSONObject("moduleBooleans") ?: JSONObject()
        val floats = root.optJSONObject("moduleFloats") ?: JSONObject()
        val colors = root.optJSONObject("moduleColors") ?: JSONObject()
        val texts = root.optJSONObject("moduleTexts") ?: JSONObject()
        val ints = root.optJSONObject("moduleInts") ?: JSONObject()
        val importedModuleBooleans = ModuleBooleanSettings.associate { spec ->
            spec.key to module.optBoolean(spec.key, spec.defaultValue)
        }
        val importedModuleFloats = ModuleFloatSettings.associate { spec ->
            spec.key to floats.optDouble(spec.key, spec.defaultValue.toDouble()).toFloat()
        }
        val importedModuleColors = ModuleColorSettings.associate { spec ->
            spec.key to colors.optInt(spec.key, spec.defaultValue)
        }
        val importedModuleTexts = ModuleTextSettings.associate { spec ->
            spec.key to texts.optString(spec.key, spec.defaultValue)
        } + (ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH to texts.optString(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH, ""))
        val importedModuleInts = ModuleIntSettings.associate { spec ->
            spec.key to ints.optInt(spec.key, spec.defaultValue).coerceIn(spec.valueRange)
        }

        val snapshot = SettingsSnapshot(
            themeMode = runCatching {
                ThemeMode.valueOf(root.optString("themeMode", ThemeMode.SYSTEM.name))
            }.getOrDefault(ThemeMode.SYSTEM),
            floatingNavEnabled = root.optBoolean("floatingNavEnabled", false),
            liquidGlassEnabled = root.optBoolean("liquidGlassEnabled", true),
            glassCardOpacity = root
                .optDouble("glassCardOpacity", AppPreferences.DEFAULT_GLASS_CARD_OPACITY.toDouble())
                .toFloat()
                .coerceIn(AppPreferences.MIN_GLASS_CARD_OPACITY, AppPreferences.MAX_GLASS_CARD_OPACITY),
            titleTextContrast = root
                .optDouble("titleTextContrast", AppPreferences.DEFAULT_TEXT_CONTRAST.toDouble())
                .toFloat()
                .coerceIn(AppPreferences.MIN_TEXT_CONTRAST, AppPreferences.MAX_TEXT_CONTRAST),
            descriptionTextContrast = root
                .optDouble("descriptionTextContrast", AppPreferences.DEFAULT_TEXT_CONTRAST.toDouble())
                .toFloat()
                .coerceIn(AppPreferences.MIN_TEXT_CONTRAST, AppPreferences.MAX_TEXT_CONTRAST),
            moduleBooleans = importedModuleBooleans,
            moduleFloats = importedModuleFloats,
            moduleColors = importedModuleColors,
            moduleTexts = importedModuleTexts,
            moduleInts = importedModuleInts,
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
    val glassCardOpacity: Float,
    val titleTextContrast: Float,
    val descriptionTextContrast: Float,
    val moduleBooleans: Map<String, Boolean>,
    val moduleFloats: Map<String, Float>,
    val moduleColors: Map<String, Int>,
    val moduleTexts: Map<String, String>,
    val moduleInts: Map<String, Int>,
)
