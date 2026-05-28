package me.inkdye.zuxos.hook.packageinstaller

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Window
import android.widget.ListAdapter
import android.widget.ListView
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook

private val packageInstallerPackages = arrayOf(
    "com.android.packageinstaller",
    "com.google.android.packageinstaller",
)

private fun PackageParam.loadInstallerHooks(block: PackageParam.() -> Unit) {
    packageInstallerPackages.forEach { loadApp(it) { block() } }
}

object PackageInstallerDisableScanHook : Hook {
    override val prefKey = ModuleConfig.PACKAGE_INSTALLER_DISABLE_SCAN
    override val targetPackage = "com.android.packageinstaller"

    override fun PackageParam.onHook() = loadInstallerHooks {
        "com.android.packageinstaller.PackageInstallerActivityExtra".toClass().method {
            name = "startScanApps"
        }.hook {
            before {
                result = null
                val activity = instance ?: return@before
                runCatching {
                    val handler = activity.javaClass.getDeclaredField("mHander").apply { isAccessible = true }
                        .get(activity)
                    handler?.javaClass?.getMethod("sendEmptyMessage", Int::class.javaPrimitiveType)
                        ?.invoke(handler, 2)
                }
            }
        }

        "com.android.packageinstaller.PackageInstallerActivityExtra".toClass().method {
            name = "showResultIfFinish"
        }.hook {
            before {
                val activity = instance ?: return@before
                runCatching {
                    activity.javaClass.getDeclaredField("mScanAppResult").apply { isAccessible = true }.setInt(activity, 2)
                    activity.javaClass.getDeclaredField("mCheckSafeInstallResult").apply { isAccessible = true }.setInt(activity, 1)
                    activity.javaClass.getDeclaredField("isScanBegin").apply { isAccessible = true }.setBoolean(activity, true)
                }
            }
        }

        "com.android.packageinstaller.PackageInstallerActivityExtra".toClass().method {
            name = "bindSafeService"
        }.hook {
            before {
                val activity = instance ?: return@before
                runCatching {
                    activity.javaClass.getDeclaredField("isBind").apply { isAccessible = true }.setBoolean(activity, true)
                    activity.javaClass.getDeclaredField("isConnect").apply { isAccessible = true }.setBoolean(activity, true)
                    val handler = activity.javaClass.getDeclaredField("mHander").apply { isAccessible = true }.get(activity)
                    handler?.javaClass?.getMethod("sendEmptyMessage", Int::class.javaPrimitiveType)
                        ?.invoke(handler, 1)
                }
                result = null
            }
        }
    }
}

object PackageInstallerAllowPermissionHook : Hook {
    override val prefKey = ModuleConfig.PACKAGE_INSTALLER_ALWAYS_ALLOW_PERMISSION
    override val targetPackage = "com.android.packageinstaller"

    override fun PackageParam.onHook() = loadInstallerHooks {
        "com.android.packageinstaller.PackageInstallerActivityExtra".toClass().method {
            name = "startCustomInstallConfirm"
        }.hook {
            after {
                val activity = instance ?: return@after
                runCatching {
                    val adapter = activity.javaClass.getDeclaredField("mAdapter").apply { isAccessible = true }.get(activity) ?: return@runCatching
                    keepOnlyTrustPermissionItem(adapter)
                    setIntField(adapter, "selectId", 2)
                    setIntField(activity, "mSelectId", 2)
                    runCatching { adapter.javaClass.getMethod("notifyDataSetChanged").invoke(adapter) }
                }
            }
        }

        "com.android.packageinstaller.extra.PermissionsAdapter".toClass().constructor {
            param(LayoutInflater::class.java, ArrayList::class.java, Handler::class.java)
        }.hook {
            after {
                val data = args(1).any() as? MutableList<Any> ?: return@after
                keepOnlyTrustPermissionItem(data)
                setIntField(instance, "selectId", 2)
            }
        }

        "com.android.packageinstaller.extra.PermissionsAdapter".toClass().method {
            name = "getCount"
        }.hook {
            after { result = 1 }
        }

        ListView::class.java.method {
            name = "setAdapter"
            param(ListAdapter::class.java)
        }.hook {
            before {
                val adapter = args(0).any() ?: return@before
                if (adapter.javaClass.name.contains("PermissionsAdapter")) {
                    setIntField(adapter, "selectId", 2)
                }
            }
        }
    }

    private fun keepOnlyTrustPermissionItem(adapter: Any) {
        val data = adapter.javaClass.declaredFields.firstOrNull { ArrayList::class.java.isAssignableFrom(it.type) }
            ?.apply { isAccessible = true }
            ?.get(adapter) as? MutableList<Any> ?: return
        keepOnlyTrustPermissionItem(data)
    }

    private fun keepOnlyTrustPermissionItem(data: MutableList<Any>) {
        val trustItem = data.firstOrNull { item ->
            runCatching {
                item.javaClass.getDeclaredField("index").apply { isAccessible = true }.getInt(item) == 2
            }.getOrDefault(false)
        } ?: return
        data.clear()
        data.add(trustItem)
    }

    private fun setIntField(target: Any?, name: String, value: Int) {
        if (target == null) return
        runCatching {
            target.javaClass.getDeclaredField(name).apply { isAccessible = true }.setInt(target, value)
        }
    }
}

object PackageInstallerSkipWarnPageHook : Hook {
    override val prefKey = ModuleConfig.PACKAGE_INSTALLER_SKIP_WARN_PAGE
    override val targetPackage = "com.android.packageinstaller"

    override fun PackageParam.onHook() = loadInstallerHooks {
        "com.android.packageinstaller.PackageInstallerActivityExtra".toClass().method {
            name = "onResume"
        }.hook {
            after {
                val activity = instance ?: return@after
                runCatching {
                    val method = activity.javaClass.methods.firstOrNull { it.name == "handleDirectInstallInFindSameAppCase" }
                        ?: activity.javaClass.methods.firstOrNull { it.name == "onDirectInstall" }
                    method?.invoke(activity)
                }
            }
        }
    }
}

object PackageInstallerDisableInstallerAdHook : Hook {
    override val prefKey = ModuleConfig.PACKAGE_INSTALLER_DISABLE_INSTALLER_AD
    override val targetPackage = "com.android.packageinstaller"

    override fun PackageParam.onHook() = loadInstallerHooks {
        "com.android.packageinstaller.InstallSuccessExtra".toClass().method {
            name = "initRecommendAppsData"
        }.hook {
            before {
                result = null
            }
        }
    }
}

object PackageInstallerStyleHook : Hook {
    override val prefKey = ModuleConfig.PACKAGE_INSTALLER_STYLE_HOOK
    override val targetPackage = "com.android.packageinstaller"

    override fun PackageParam.onHook() = loadInstallerHooks {
        "com.android.packageinstaller.extra.Utils".toClass().method {
            name = "isCTSandGTS"
            param(String::class.java)
        }.hook {
            replaceAny { true }
        }

        "com.android.packageinstaller.extra.Utils".toClass().method {
            name = "isCTSandGTS"
            param(String::class.java, Intent::class.java)
        }.hook {
            replaceAny { true }
        }

        Activity::class.java.method {
            name = "onCreate"
            param(Bundle::class.java)
        }.hook {
            before {
                val activity = instance as? Activity ?: return@before
                if (activity.packageName !in packageInstallerPackages) return@before
                runCatching {
                    val styleClass = Class.forName(
                        "com.android.packageinstaller.R\$style",
                        false,
                        activity.javaClass.classLoader,
                    )
                    val theme = styleClass.getDeclaredField("Theme_AlertDialogActivity").getInt(null)
                    activity.setTheme(theme)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        runCatching {
                            Activity::class.java.getMethod("setTranslucent", Boolean::class.javaPrimitiveType)
                                .invoke(activity, true)
                        }
                    }
                    activity.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    activity.window?.setWindowAnimations(0)
                }
            }
        }
    }
}

object PackageInstallerDisableDeleteHook : Hook {
    override val prefKey = ModuleConfig.PACKAGE_INSTALLER_DISABLE_DELETE
    override val targetPackage = "com.android.packageinstaller"

    override fun PackageParam.onHook() = loadInstallerHooks {
        "com.android.packageinstaller.InstallSuccessExtra".toClass().method {
            name = "initView"
        }.hook {
            after {
                val activity = instance ?: return@after
                runCatching {
                    activity.javaClass.getDeclaredField("isConfigChanage").apply { isAccessible = true }.getBoolean(activity)
                        .takeIf { !it } ?: return@runCatching
                    activity.javaClass.getDeclaredField("mDeleteApk").apply { isAccessible = true }.setBoolean(activity, false)
                    val cb = activity.javaClass.getDeclaredField("del_check_box").apply { isAccessible = true }.get(activity)
                    if (cb is android.widget.CheckBox) cb.isChecked = false
                }
            }
        }
    }
}
