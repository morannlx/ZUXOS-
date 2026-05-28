package me.inkdye.zuxos.application

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.ui.settings.ModuleBooleanSettings
import me.inkdye.zuxos.ui.settings.AppPreferences
import me.inkdye.zuxos.ui.theme.ThemeMode

class DefaultApplication : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()

        // Apply saved theme mode
        val savedMode = AppPreferences.getThemeMode(this)
        val nightMode = when (savedMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        DynamicColors.applyToActivitiesIfAvailable(this)

        ModuleBooleanSettings.forEach { spec ->
            if (!ModuleConfig.contains(this, spec.key)) {
                ModuleConfig.saveBoolean(this, spec.key, spec.defaultValue)
            }
        }
    }
}
