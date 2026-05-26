package me.inkdye.zuxos.application

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication

class DefaultApplication : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        DynamicColors.applyToActivitiesIfAvailable(this)

        // 初始化 SharedPreferences，确保 ModuleConfig 能读取
        val prefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        if (!prefs.contains("app_info_show_package_name")) {
            prefs.edit()
                .putBoolean("app_info_show_package_name", false)
                .putBoolean("app_info_show_sdk_version", false)
                .putBoolean("app_info_show_install_time", false)
                .putBoolean("app_info_show_update_time", false)
                .putBoolean("app_info_show_install_source", false)
                .putBoolean("app_info_enable_long_copy", false)
                .apply()
        }
    }
}
