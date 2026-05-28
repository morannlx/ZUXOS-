package me.inkdye.zuxos.logging

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig

class HookLogProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val ctx = context ?: return null
        if (uri.path != PATH_APPEND || values == null) return null
        if (!ModuleConfig.loadBoolean(ctx, ModuleConfig.HOOK_LOGGING_ENABLED, false)) return null
        val entry = HookLogEntry(
            timestamp = values.getAsLong(KEY_TIMESTAMP) ?: System.currentTimeMillis(),
            hookName = values.getAsString(KEY_HOOK_NAME).orEmpty(),
            moduleName = values.getAsString(KEY_MODULE_NAME).orEmpty(),
            targetPackage = values.getAsString(KEY_TARGET_PACKAGE).orEmpty(),
            processName = values.getAsString(KEY_PROCESS_NAME).orEmpty(),
            stage = runCatching { HookLogStage.valueOf(values.getAsString(KEY_STAGE).orEmpty()) }
                .getOrDefault(HookLogStage.EXCEPTION),
            level = runCatching { HookLogLevel.valueOf(values.getAsString(KEY_LEVEL).orEmpty()) }
                .getOrDefault(HookLogLevel.INFO),
            message = values.getAsString(KEY_MESSAGE).orEmpty(),
            stackTrace = values.getAsString(KEY_STACK_TRACE),
        )
        if (!isAllowedCaller(entry)) return null
        HookLogStore.append(ctx, entry)
        return uri
    }

    private fun isAllowedCaller(entry: HookLogEntry): Boolean {
        val callerPackages = buildSet {
            runCatching { callingPackage }.getOrNull()?.let(::add)
            context?.packageManager
                ?.getPackagesForUid(Binder.getCallingUid())
                ?.forEach(::add)
        }
        return callerPackages.any { caller ->
            caller == BuildConfig.APPLICATION_ID ||
                caller == entry.targetPackage ||
                caller in ALLOWED_HOOK_PACKAGES
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.hooklogs"
        private const val PATH_APPEND = "/append"
        private val ALLOWED_HOOK_PACKAGES = setOf(
            "com.android.settings",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
        )
        val APPEND_URI: Uri = Uri.parse("content://$AUTHORITY$PATH_APPEND")

        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_HOOK_NAME = "hookName"
        const val KEY_MODULE_NAME = "moduleName"
        const val KEY_TARGET_PACKAGE = "targetPackage"
        const val KEY_PROCESS_NAME = "processName"
        const val KEY_STAGE = "stage"
        const val KEY_LEVEL = "level"
        const val KEY_MESSAGE = "message"
        const val KEY_STACK_TRACE = "stackTrace"
    }
}
