package me.inkdye.zuxos.hook.launcher

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook

object LauncherGridHook : Hook {
    override val prefKey = ModuleConfig.LAUNCHER_CUSTOM_GRID
    override val targetPackage = LauncherConstants.PACKAGE_ZUX_LAUNCHER
    override val moduleName = "zux_launcher_grid"

    private const val TAG = "ZUXOS.LauncherGrid"

    override fun PackageParam.onHook() {
        runCatching {
            val gridOptionClass = XposedHelpers.findClassIfExists(
                "com.android.launcher3.InvariantDeviceProfile\$GridOption",
                hostClassLoader,
            ) ?: return
            val displayInfoClass = XposedHelpers.findClassIfExists(
                "com.android.launcher3.util.DisplayController\$Info",
                hostClassLoader,
            ) ?: return

            XposedHelpers.findAndHookConstructor(
                gridOptionClass,
                Context::class.java,
                AttributeSet::class.java,
                displayInfoClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val rows = ModuleConfig.readInt(
                            ModuleConfig.LAUNCHER_CUSTOM_GRID_ROWS,
                            LauncherConstants.DEFAULT_GRID_ROWS,
                        ).coerceIn(LauncherConstants.MIN_GRID_SIZE, LauncherConstants.MAX_GRID_SIZE)
                        val columns = ModuleConfig.readInt(
                            ModuleConfig.LAUNCHER_CUSTOM_GRID_COLUMNS,
                            LauncherConstants.DEFAULT_GRID_COLUMNS,
                        ).coerceIn(LauncherConstants.MIN_GRID_SIZE, LauncherConstants.MAX_GRID_SIZE)
                        param.thisObject?.setIntField("numRows", rows)
                        param.thisObject?.setIntField("numColumns", columns)
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook GridOption constructor failed", it) }
    }

    private fun Any.setIntField(name: String, value: Int) {
        runCatching { XposedHelpers.setIntField(this, name, value) }
            .onFailure { Log.w(TAG, "Set field failed: $name", it) }
    }
}
