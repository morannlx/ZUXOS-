package me.inkdye.zuxos.hook.lenovo

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import me.inkdye.zuxos.hook.hookFirstMethod

object LenovoSystemUpdateLocalInstallHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_SYSTEM_UPDATE_FORCE_LOCAL_INSTALL
    override val targetPackage = PACKAGE_LENOVO_OTA
    override val moduleName = "lenovo_system_update"

    private const val PACKAGE_LENOVO_OTA = "com.lenovo.ota"
    private val MAIN_ACTIVITY_CANDIDATES = listOf(
        "com.lenovo.row.ota.core.d.ui.MainActivity",
        "com.lenovo.row.ota.core.p007d.p008ui.MainActivity",
    )
    private const val LOCAL_INSTALL_MENU_ID = 2131296583
    private const val LOCAL_INSTALL_MENU_NAME = "memu_localInstall"

    override fun PackageParam.onHook() {
        hookFirstMethod(MenuItem::class.java, "setVisible", Boolean::class.javaPrimitiveType!!) {
            before {
                val item = instance as? MenuItem ?: return@before
                if (item.itemId == LOCAL_INSTALL_MENU_ID && args[0] as? Boolean == false) result = item.setVisible(true)
            }
        }

        MAIN_ACTIVITY_CANDIDATES.forEach { mainActivity ->
            runCatching {
                hookFirstMethod(mainActivity, "onCreateOptionsMenu", Menu::class.java) {
                    after {
                        val activity = instance as? Activity ?: return@after
                        (args[0] as? Menu)?.showLocalInstallItem(activity.localInstallMenuId())
                    }
                }
            }

            runCatching {
                hookFirstMethod(mainActivity, "onPrepareOptionsMenu", Menu::class.java) {
                    after {
                        val activity = instance as? Activity ?: return@after
                        (args[0] as? Menu)?.showLocalInstallItem(activity.localInstallMenuId())
                        instance.setLocalInstallClickCount()
                    }
                }
            }

            runCatching {
                hookFirstMethod(mainActivity, "clickCountCallBack") {
                    before {
                        instance.setLocalInstallClickCount()
                    }
                }
            }
        }
    }

    private fun Activity.localInstallMenuId(): Int {
        return resources.getIdentifier(LOCAL_INSTALL_MENU_NAME, "id", PACKAGE_LENOVO_OTA)
            .takeIf { it != 0 } ?: LOCAL_INSTALL_MENU_ID
    }

    private fun Menu.showLocalInstallItem(itemId: Int) {
        findItem(itemId)?.apply {
            isVisible = true
            isEnabled = true
        }
    }

    private fun Any.setLocalInstallClickCount() {
        runCatching {
            javaClass.getDeclaredField("mCount")
                .apply { isAccessible = true }
                .setInt(this, 6)
        }
    }
}
