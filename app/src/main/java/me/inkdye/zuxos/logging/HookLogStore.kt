package me.inkdye.zuxos.logging

import android.content.Context
import java.io.File

object HookLogStore {
    private const val LOG_DIR = "hook_logs"
    private const val LOG_FILE = "hook-events.jsonl"
    private const val MAX_BYTES = 512 * 1024
    private const val TRIM_TO_BYTES = 384 * 1024

    private val lock = Any()

    fun append(context: Context, entry: HookLogEntry) {
        synchronized(lock) {
            val file = logFile(context)
            file.parentFile?.mkdirs()
            file.appendText(entry.toJsonLine() + "\n")
            trimIfNeeded(file)
        }
    }

    fun read(context: Context): List<HookLogEntry> = synchronized(lock) {
        val file = logFile(context)
        if (!file.exists()) return@synchronized emptyList()
        file.readLines()
            .asSequence()
            .mapNotNull(HookLogEntry::fromJsonLine)
            .toList()
            .asReversed()
    }

    fun clear(context: Context) {
        synchronized(lock) {
            logFile(context).delete()
        }
    }

    private fun logFile(context: Context): File =
        File(File(context.filesDir, LOG_DIR), LOG_FILE)

    private fun trimIfNeeded(file: File) {
        if (file.length() <= MAX_BYTES) return
        val bytes = file.readBytes()
        val keepFrom = (bytes.size - TRIM_TO_BYTES).coerceAtLeast(0)
        val kept = bytes.copyOfRange(keepFrom, bytes.size).decodeToString()
        file.writeText(kept.substringAfter('\n', kept))
    }
}
