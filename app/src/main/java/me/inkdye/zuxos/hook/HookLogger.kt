package me.inkdye.zuxos.hook

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.logging.HookLogLevel
import me.inkdye.zuxos.logging.HookLogProvider
import me.inkdye.zuxos.logging.HookLogStage

object HookLogger {
    private const val TAG = "ZUXOS.HookLogger"
    private const val MAX_PENDING_LOGS = 128

    private val lock = Any()
    private val contextHookedProcesses = mutableSetOf<String>()
    private val pendingLogs = ArrayDeque<ContentValues>()

    fun log(
        param: PackageParam?,
        hook: Hook,
        stage: HookLogStage,
        message: String,
        level: HookLogLevel = HookLogLevel.INFO,
        throwable: Throwable? = null,
    ) {
        if (!ModuleConfig.isEnabled(ModuleConfig.HOOK_LOGGING_ENABLED, false)) return
        runCatching {
            val values = ContentValues().apply {
                put(HookLogProvider.KEY_TIMESTAMP, System.currentTimeMillis())
                put(HookLogProvider.KEY_HOOK_NAME, hook.name)
                put(HookLogProvider.KEY_MODULE_NAME, hook.moduleName)
                put(HookLogProvider.KEY_TARGET_PACKAGE, param?.packageName.orEmpty())
                put(HookLogProvider.KEY_PROCESS_NAME, param?.processName.orEmpty())
                put(HookLogProvider.KEY_STAGE, stage.name)
                put(HookLogProvider.KEY_LEVEL, level.name)
                put(HookLogProvider.KEY_MESSAGE, message)
                put(HookLogProvider.KEY_STACK_TRACE, throwable?.stackTraceToString())
            }
            val context = param?.hostApplication
            if (context != null) {
                write(context, values)
            } else {
                enqueue(values)
            }
        }.onFailure {
            Log.w(TAG, "Failed to write hook log", it)
        }
    }

    fun installContextBridge(param: PackageParam) {
        if (!ModuleConfig.isEnabled(ModuleConfig.HOOK_LOGGING_ENABLED, false)) return
        val processKey = "${param.packageName}:${param.processName}"
        synchronized(lock) {
            if (!contextHookedProcesses.add(processKey)) return
        }
        runCatching {
            XposedHelpers.findAndHookMethod(
                android.app.Application::class.java,
                "attach",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val context = param.args.firstOrNull() as? Context ?: return
                        flush(context)
                    }
                }
            )
        }.onFailure {
            Log.w(TAG, "Failed to install hook log context bridge", it)
        }
    }

    private fun enqueue(values: ContentValues) = synchronized(lock) {
        while (pendingLogs.size >= MAX_PENDING_LOGS) pendingLogs.removeFirst()
        pendingLogs.addLast(ContentValues(values))
    }

    private fun flush(context: Context) {
        val logs = synchronized(lock) {
            if (pendingLogs.isEmpty()) return
            buildList {
                while (pendingLogs.isNotEmpty()) add(pendingLogs.removeFirst())
            }
        }
        logs.forEach { values -> write(context, values) }
    }

    private fun write(context: Context, values: ContentValues) {
        context.contentResolver.insert(HookLogProvider.APPEND_URI, values)
    }
}
