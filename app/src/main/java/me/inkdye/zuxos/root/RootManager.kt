package me.inkdye.zuxos.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

enum class RootStatus {
    UNKNOWN,
    UNAVAILABLE,
    DENIED,
    GRANTED,
}

enum class RestartScope(val label: String, internal val command: String) {
    SETTINGS("Settings", "am force-stop com.android.settings"),
    PACKAGE_INSTALLER(
        "Package Installer",
        "am force-stop com.android.packageinstaller; am force-stop com.google.android.packageinstaller",
    ),
    LENOVO_SYSTEM_UPDATE(
        "Lenovo System Update",
        "am force-stop com.lenovo.ota; am force-stop com.android.settings; am force-stop com.zui.launcher",
    ),
    LENOVO_GAME_ASSISTANT(
        "Lenovo Game Assistant",
        "am force-stop com.zui.game.service; am force-stop com.android.settings",
    ),
    ZUX_LAUNCHER("ZUX Launcher", "am force-stop com.zui.launcher"),
    SYSTEMUI("SystemUI", "pkill -f com.android.systemui || kill $(pidof com.android.systemui)"),
    ANDROID_SYSTEM("Android System", "kill $(pidof system_server)"),
}

data class RootState(
    val status: RootStatus,
    val message: String? = null,
) {
    val isGranted: Boolean get() = status == RootStatus.GRANTED
}

data class RootCommandResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String,
    val error: String,
) {
    val message: String
        get() = when {
            success -> output.ifBlank { "Command completed" }
            error.isNotBlank() -> error
            output.isNotBlank() -> output
            else -> "Command failed with exit code $exitCode"
        }
}

object RootManager {
    private const val DEFAULT_TIMEOUT_SECONDS = 12L
    private val suPaths = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/data/adb/ksu/bin/su",
        "/debug_ramdisk/su",
    )

    suspend fun queryState(): RootState = withContext(Dispatchers.IO) {
        if (!hasSuBinary()) return@withContext RootState(RootStatus.UNAVAILABLE, "su binary not found")
        val result = runSu("id", timeoutSeconds = 3)
        when {
            result.success && result.output.contains("uid=0") -> RootState(RootStatus.GRANTED)
            result.exitCode == 124 -> RootState(RootStatus.UNKNOWN, "Root request timed out")
            else -> RootState(RootStatus.DENIED, result.message)
        }
    }

    suspend fun requestAccess(): RootState = withContext(Dispatchers.IO) {
        if (!hasSuBinary()) return@withContext RootState(RootStatus.UNAVAILABLE, "su binary not found")
        val result = runSu("id")
        when {
            result.success && result.output.contains("uid=0") -> RootState(RootStatus.GRANTED)
            result.exitCode == 124 -> RootState(RootStatus.UNKNOWN, "Root request timed out")
            else -> RootState(RootStatus.DENIED, result.message)
        }
    }

    suspend fun restart(scope: RestartScope): RootCommandResult = withContext(Dispatchers.IO) {
        runSu(scope.command)
    }

    private fun hasSuBinary(): Boolean {
        if (suPaths.any { File(it).canExecute() || File(it).exists() }) return true
        return runCatching {
            val process = ProcessBuilder("sh", "-c", "command -v su")
                .redirectErrorStream(true)
                .start()
            process.waitFor(2, TimeUnit.SECONDS) && process.exitValue() == 0
        }.getOrDefault(false)
    }

    private fun runSu(command: String, timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS): RootCommandResult {
        return runCatching {
            val process = ProcessBuilder("su", "-c", command).start()
            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return@runCatching RootCommandResult(
                    success = false,
                    exitCode = 124,
                    output = "",
                    error = "Root command timed out",
                )
            }
            val output = process.inputStream.bufferedReader().readText().trim()
            val error = process.errorStream.bufferedReader().readText().trim()
            val exitCode = process.exitValue()
            RootCommandResult(
                success = exitCode == 0,
                exitCode = exitCode,
                output = output,
                error = error,
            )
        }.getOrElse { error ->
            RootCommandResult(
                success = false,
                exitCode = -1,
                output = "",
                error = error.localizedMessage ?: error.javaClass.simpleName,
            )
        }
    }
}
