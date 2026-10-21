package me.inkdye.zuxos.hook.lenovo

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import me.inkdye.zuxos.hook.hookFirstMethod

object LenovoSystemUpdateReminderHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_SYSTEM_UPDATE_HIDE_NEW_VERSION_REMINDER
    override val targetPackage = PACKAGE_LENOVO_OTA
    override val targetPackages = setOf(PACKAGE_LENOVO_OTA, PACKAGE_SETTINGS)
    override val moduleName = "lenovo_system_update"

    private const val PACKAGE_LENOVO_OTA = "com.lenovo.ota"
    private const val PACKAGE_SETTINGS = "com.android.settings"
    private const val OTA_NEW_VERSION_FOUND = "lenovo_ota_new_version_found"
    private const val BADGE_URI = "content://com.android.badge/badge"
    private const val BADGE_METHOD_SET_COUNT = "setAppBadgeCount"
    private const val BADGE_KEY_COUNT = "app_badge_count"
    private const val BADGE_KEY_PACKAGE = "app_package_name"
    private const val BADGE_KEY_CLASS = "app_class_name"
    private const val OTA_MAIN_ACTIVITY = "com.lenovo.row.ota.core.d.ui.MainActivity"
    private const val OTA_MAIN_ACTIVITY_JADX = "com.lenovo.row.ota.core.p007d.p008ui.MainActivity"
    private const val SETTINGS_LAUNCHER_ACTIVITY = "com.android.settings.Settings"
    private val OTA_PACKAGE_INFO_CANDIDATES = listOf(
        "com.lenovo.row.ota.core.a.otapackage.OtaPackageInfo",
        "com.lenovo.row.ota.core.p003a.otapackage.OtaPackageInfo",
    )
    private val SERVICE_CONTROLLER_CANDIDATES = listOf(
        "com.lenovo.row.ota.core.d.services.ServiceController",
        "com.lenovo.row.ota.core.p007d.services.ServiceController",
    )
    private val NOTIFICATION_CENTER_CANDIDATES = listOf(
        "com.lenovo.row.ota.core.d.notification.NotificationCenter",
        "com.lenovo.row.ota.core.p007d.notification.NotificationCenter",
    )
    private val MAIN_ACTIVITY_CANDIDATES = listOf(
        OTA_MAIN_ACTIVITY,
        OTA_MAIN_ACTIVITY_JADX,
    )
    private val WORKFLOW_MANAGER_CANDIDATES = listOf(
        "com.lenovo.row.ota.core.a.workflow.WorkFlowManager",
        "com.lenovo.row.ota.core.p003a.workflow.WorkFlowManager",
    )

    override fun PackageParam.onHook() {
        if (packageName == PACKAGE_LENOVO_OTA) hookLenovoOta()
        if (packageName == PACKAGE_SETTINGS) hookSettingsEntryBadges()
        hookLauncherBadgeProvider()
    }

    private fun PackageParam.hookLenovoOta() {
        hookFirstMethod("com.lenovo.row.ota.MyApplication", "onCreate") {
            after {
                (instance as? Context)?.clearOtaNewVersionState()
            }
        }

        OTA_PACKAGE_INFO_CANDIDATES.forEach { className ->
            runCatching {
                hookFirstMethod(className, "isUpdateFound") {
                    intercept(false)
                }
            }
        }

        SERVICE_CONTROLLER_CANDIDATES.forEach { className ->
            runCatching {
                hookFirstMethod(className, "startNewVersionFound", Context::class.java) {
                    before {
                        (args[0] as? Context)?.clearOtaNewVersionState()
                        result = null
                    }
                }
            }
        }

        NOTIFICATION_CENTER_CANDIDATES.forEach { className ->
            runCatching {
                hookFirstMethod(className, "showNewVersionNotification") {
                    before { result = null }
                }
            }
        }

        MAIN_ACTIVITY_CANDIDATES.forEach { className ->
            runCatching {
                hookFirstMethod(className, "showFragNewVersionFound") {
                    before {
                        result = null
                        runCatching {
                            instance.javaClass.getDeclaredMethod("showFragCheckingNewVersion")
                                .apply { isAccessible = true }
                                .invoke(instance)
                        }
                    }
                }
            }
        }

        WORKFLOW_MANAGER_CANDIDATES.forEach { className ->
            runCatching {
                hookFirstMethod(className, "reddot", Context::class.java, Int::class.javaPrimitiveType!!) {
                    before {
                        if ((args[1] as? Int ?: 0) > 0) {
                            (args[0] as? Context)?.clearOtaNewVersionState()
                            result = false
                        }
                    }
                }
            }
        }
    }

    private fun PackageParam.hookSettingsEntryBadges() {
        hookFirstMethod("com.android.settings.SettingsApplication", "onCreate") {
            after {
                (instance as? Context)?.clearOtaNewVersionState()
            }
        }

        hookFirstMethod(
            Settings.Secure::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.javaPrimitiveType!!,
        ) {
            before {
                if (args[1] as? String == OTA_NEW_VERSION_FOUND) result = 0
            }
        }
    }

    private fun PackageParam.hookLauncherBadgeProvider() {
        hookFirstMethod(
            ContentResolver::class.java,
            "call",
            Uri::class.java,
            String::class.java,
            String::class.java,
            Bundle::class.java,
        ) {
            before {
                val uri = args[0] as? Uri ?: return@before
                val method = args[1] as? String ?: return@before
                val extras = args[3] as? Bundle ?: return@before
                if (uri.toString() != BADGE_URI || method != BADGE_METHOD_SET_COUNT) return@before
                if (!extras.isSystemUpdateBadgeTarget()) return@before
                extras.putInt(BADGE_KEY_COUNT, 0)
            }
        }
    }

    private fun Context.clearOtaNewVersionState() {
        runCatching {
            getSharedPreferences("otaPackageBrief", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("isUpdateFound", false)
                .apply()
        }
        runCatching {
            Settings.Secure.putInt(contentResolver, OTA_NEW_VERSION_FOUND, 0)
        }
        clearLauncherBadge()
    }

    private fun Context.clearLauncherBadge() {
        clearLauncherBadge(PACKAGE_LENOVO_OTA, OTA_MAIN_ACTIVITY)
        clearLauncherBadge(PACKAGE_LENOVO_OTA, OTA_MAIN_ACTIVITY_JADX)
        clearLauncherBadge(PACKAGE_SETTINGS, SETTINGS_LAUNCHER_ACTIVITY)
    }

    private fun Context.clearLauncherBadge(packageName: String, className: String) {
        runCatching {
            val extras = Bundle().apply {
                putStringArrayList("app_shortcut_custom_id", null)
                putInt(BADGE_KEY_COUNT, 0)
                putString(BADGE_KEY_PACKAGE, packageName)
                putString(BADGE_KEY_CLASS, className)
            }
            contentResolver.call(Uri.parse(BADGE_URI), BADGE_METHOD_SET_COUNT, null, extras)
        }
    }

    private fun Bundle.isSystemUpdateBadgeTarget(): Boolean {
        val targetPackage = getString(BADGE_KEY_PACKAGE)
        val targetClass = getString(BADGE_KEY_CLASS)
        return targetPackage == PACKAGE_LENOVO_OTA ||
            targetPackage == PACKAGE_SETTINGS ||
            targetClass == OTA_MAIN_ACTIVITY ||
            targetClass == OTA_MAIN_ACTIVITY_JADX ||
            targetClass == SETTINGS_LAUNCHER_ACTIVITY
    }
}
