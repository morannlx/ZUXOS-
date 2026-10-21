package me.inkdye.zuxos.hook.launcher

import android.util.Log
import android.view.View
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook

object LauncherHideIconLabelHook : Hook {
    override val prefKey = ModuleConfig.LAUNCHER_HIDE_ICON_LABEL
    override val targetPackage = LauncherConstants.PACKAGE_ZUX_LAUNCHER
    override val moduleName = "zux_launcher_hide_icon_label"

    private const val TAG = "ZUXOS.LauncherIconLabel"
    private const val DISPLAY_WORKSPACE = 0
    private const val DISPLAY_FOLDER = 2
    private val forcingTextAlpha = ThreadLocal.withInitial { false }

    override fun PackageParam.onHook() {
        val bubbleTextViewClass = XposedHelpers.findClassIfExists(
            "com.android.launcher3.BubbleTextView",
            hostClassLoader,
        ) ?: return
        val activeIconViewClass = XposedHelpers.findClassIfExists(
            "com.zui.launcher.ActiveIconView",
            hostClassLoader,
        )
        val folderIconClass = XposedHelpers.findClassIfExists(
            "com.android.launcher3.folder.FolderIcon",
            hostClassLoader,
        )
        val appPairIconClass = XposedHelpers.findClassIfExists(
            "com.android.launcher3.apppairs.AppPairIcon",
            hostClassLoader,
        )

        hookWorkspaceIconClass(
            className = "BubbleTextView",
            iconClass = bubbleTextViewClass,
            itemInfoClassName = "com.android.launcher3.model.data.WorkspaceItemInfo",
            hookApplyLabel = true,
        )
        activeIconViewClass?.let {
            hookWorkspaceIconClass(
                className = "ActiveIconView",
                iconClass = it,
                itemInfoClassName = "com.android.launcher3.model.data.WorkspaceItemInfo",
                hookApplyLabel = false,
            )
        }
        folderIconClass?.let { hookFolderIconClass(it) }
        appPairIconClass?.let { hookAppPairIconClass(it) }
    }

    private fun hookWorkspaceIconClass(
        className: String,
        iconClass: Class<*>,
        itemInfoClassName: String,
        hookApplyLabel: Boolean,
    ) {
        runHook("$className.applyFromWorkspaceItem") {
            XposedHelpers.findAndHookMethod(
                iconClass,
                "applyFromWorkspaceItem",
                itemInfoClassName,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideWorkspaceLabelIfNeeded()
                    }
                },
            )
        }

        if (hookApplyLabel) runHook("$className.applyLabel") {
            XposedHelpers.findAndHookMethod(
                iconClass,
                "applyLabel",
                "com.android.launcher3.model.data.ItemInfo",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideWorkspaceLabelIfNeeded(skipIfAlphaAlreadyZero = true)
                    }
                },
            )
        }

        runHook("$className.onAttachedToWindow") {
            XposedHelpers.findAndHookMethod(
                iconClass,
                "onAttachedToWindow",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideWorkspaceLabelIfNeeded()
                    }
                },
            )
        }

        runHook("$className.setTextAlpha") {
            XposedHelpers.findAndHookMethod(
                iconClass,
                "setTextAlpha",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideWorkspaceLabelIfNeeded()
                    }
                },
            )
        }

        runHook("$className.setTextVisibility") {
            XposedHelpers.findAndHookMethod(
                iconClass,
                "setTextVisibility",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideWorkspaceLabelIfNeeded()
                    }
                },
            )
        }

        runHook("$className.setTextColor") {
            XposedHelpers.findAndHookMethod(
                iconClass,
                "setTextColor",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideWorkspaceLabelIfNeeded()
                    }
                },
            )
        }
    }

    private fun hookFolderIconClass(folderIconClass: Class<*>) {
        runHook("FolderIcon.onAttachedToWindow") {
            XposedHelpers.findAndHookMethod(
                folderIconClass,
                "onAttachedToWindow",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideFolderIconLabelIfNeeded()
                    }
                },
            )
        }

        runHook("FolderIcon.setTextColor") {
            XposedHelpers.findAndHookMethod(
                folderIconClass,
                "setTextColor",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideFolderIconLabelIfNeeded()
                    }
                },
            )
        }

        runHook("FolderIcon.setTextVisible") {
            XposedHelpers.findAndHookMethod(
                folderIconClass,
                "setTextVisible",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideFolderIconLabelIfNeeded()
                    }
                },
            )
        }
    }

    private fun hookAppPairIconClass(appPairIconClass: Class<*>) {
        runHook("AppPairIcon.onAttachedToWindow") {
            XposedHelpers.findAndHookMethod(
                appPairIconClass,
                "onAttachedToWindow",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideAppPairIconLabelIfNeeded()
                    }
                },
            )
        }

        runHook("AppPairIcon.updateTitleAndTextView") {
            XposedHelpers.findAndHookMethod(
                appPairIconClass,
                "updateTitleAndTextView",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideAppPairIconLabelIfNeeded()
                    }
                },
            )
        }

        runHook("AppPairIcon.setNameTextColor") {
            XposedHelpers.findAndHookMethod(
                appPairIconClass,
                "setNameTextColor",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.thisObject?.hideAppPairIconLabelIfNeeded()
                    }
                },
            )
        }
    }

    private fun Any.hideWorkspaceLabelIfNeeded(skipIfAlphaAlreadyZero: Boolean = false) {
        if (!isTargetIconLabel()) return
        if (forcingTextAlpha.get() == true) return
        if (skipIfAlphaAlreadyZero && textAlphaOrNull() == 0f) return
        runCatching {
            forcingTextAlpha.set(true)
            XposedHelpers.callMethod(this, "setTextAlpha", 0f)
        }.also {
            forcingTextAlpha.set(false)
        }
            .onFailure { Log.w(TAG, "Hide workspace icon label failed", it) }
    }

    private fun Any.hideFolderIconLabelIfNeeded() {
        val folderName = runCatching { XposedHelpers.callMethod(this, "getFolderName") }.getOrNull()
            ?: return
        folderName.hideWorkspaceLabelIfNeeded()
    }

    private fun Any.hideAppPairIconLabelIfNeeded() {
        if (!isWorkspaceOrFolderContainer()) return
        val titleView = runCatching { XposedHelpers.callMethod(this, "getTitleTextView") }.getOrNull()
            ?: return
        titleView.hideWorkspaceLabelIfNeeded()
    }

    private fun Any.isTargetIconLabel(): Boolean {
        if (this !is View) return false
        return displayTypeOrNull() in setOf(DISPLAY_WORKSPACE, DISPLAY_FOLDER)
    }

    private fun Any.displayTypeOrNull(): Int? =
        runCatching { XposedHelpers.callMethod(this, "getDisplayType") as? Int }.getOrNull()
            ?: runCatching { XposedHelpers.callMethod(this, "getIconDisplay") as? Int }.getOrNull()

    private fun Any.containerOrNull(): Int? =
        runCatching { XposedHelpers.callMethod(this, "getContainer") as? Int }.getOrNull()

    private fun Any.isWorkspaceOrFolderContainer(): Boolean =
        containerOrNull() in setOf(DISPLAY_WORKSPACE, DISPLAY_FOLDER)

    private fun Any.textAlphaOrNull(): Float? =
        runCatching { XposedHelpers.callMethod(this, "getTextAlpha") as? Float }.getOrNull()

    private fun runHook(name: String, block: () -> Unit) {
        runCatching(block).onFailure { Log.w(TAG, "Hook failed: $name", it) }
    }
}
