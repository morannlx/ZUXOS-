package me.inkdye.zuxos.logging

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HookLogLevel { INFO, WARN, ERROR }

enum class HookLogStage {
    REGISTER,
    ENABLED,
    DISABLED,
    EXECUTE_START,
    EXECUTE_SUCCESS,
    EXECUTE_FAILURE,
    EXCEPTION,
}

data class HookLogEntry(
    val timestamp: Long,
    val hookName: String,
    val moduleName: String,
    val targetPackage: String,
    val processName: String,
    val stage: HookLogStage,
    val level: HookLogLevel,
    val message: String,
    val stackTrace: String? = null,
) {
    val formattedTime: String
        get() = timeFormat.get()!!.format(Date(timestamp))

    fun toJsonLine(): String = JSONObject()
        .put("timestamp", timestamp)
        .put("hookName", hookName)
        .put("moduleName", moduleName)
        .put("targetPackage", targetPackage)
        .put("processName", processName)
        .put("stage", stage.name)
        .put("level", level.name)
        .put("message", message)
        .put("stackTrace", stackTrace)
        .toString()

    companion object {
        private val timeFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        }

        fun fromJsonLine(line: String): HookLogEntry? = runCatching {
            val obj = JSONObject(line)
            HookLogEntry(
                timestamp = obj.optLong("timestamp"),
                hookName = obj.optString("hookName"),
                moduleName = obj.optString("moduleName"),
                targetPackage = obj.optString("targetPackage"),
                processName = obj.optString("processName"),
                stage = runCatching { HookLogStage.valueOf(obj.optString("stage")) }
                    .getOrDefault(HookLogStage.EXCEPTION),
                level = runCatching { HookLogLevel.valueOf(obj.optString("level")) }
                    .getOrDefault(HookLogLevel.INFO),
                message = obj.optString("message"),
                stackTrace = obj.optString("stackTrace").takeIf { it.isNotBlank() && it != "null" },
            )
        }.getOrNull()
    }
}
