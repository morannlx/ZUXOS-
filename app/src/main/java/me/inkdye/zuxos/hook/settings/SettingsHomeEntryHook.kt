package me.inkdye.zuxos.hook.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.config.ModuleConfigProvider
import me.inkdye.zuxos.hook.Hook
import java.lang.reflect.Proxy
import java.util.Locale

object SettingsHomeEntryHook : Hook {
    override val prefKey = "settings_home_zuxos_entry"
    override val alwaysRegister = true
    override val targetPackage = PACKAGE_SETTINGS
    override val moduleName = "settings_home"

    private const val TAG = "ZUXOS.SettingsHome"
    private const val PACKAGE_SETTINGS = "com.android.settings"
    private const val TOP_LEVEL_SETTINGS = "com.android.settings.homepage.TopLevelSettings"
    private const val DASHBOARD_FRAGMENT = "com.android.settings.dashboard.DashboardFragment"
    private const val SYSTEM_DASHBOARD_FRAGMENT = "com.android.settings.system.SystemDashboardFragment"
    private const val SUB_SETTINGS = "com.android.settings.SubSettings"
    private const val EXTRA_SHOW_FRAGMENT = ":settings:show_fragment"
    private const val EXTRA_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args"
    private const val EXTRA_SHOW_FRAGMENT_TITLE = ":settings:show_fragment_title"
    private const val EXTRA_SOURCE_METRICS = ":settings:source_metrics"
    private const val EXTRA_IS_FROM_SETTINGS_HOMEPAGE = "is_from_settings_homepage"
    private const val KEY_SYSTEM_CATEGORY = "top_level_system_info_category"
    private const val KEY_GENERAL = "top_level_system"
    private const val HOME_ENTRY_ORDER = -9

    override fun PackageParam.onHook() {
        val classLoader = appClassLoader
        hookTopLevelEntry(classLoader)
        hookHostSettingsPage(classLoader)
    }

    private fun hookTopLevelEntry(classLoader: ClassLoader?) {
        val clazz = XposedHelpers.findClassIfExists(TOP_LEVEL_SETTINGS, classLoader) ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreatePreferences",
            Bundle::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val fragment = param.thisObject ?: return
                    val context = fragment.call("getContext") as? Context ?: return
                    if (!loadBoolean(context, ModuleConfig.SETTINGS_HOME_ENTRY_ENABLED, true)) return
                    val screen = fragment.call("getPreferenceScreen") ?: return
                    if (screen.call("findPreference", SettingsHostDefinitions.HOME_ENTRY_KEY) != null) {
                        return
                    }

                    val entry = createHomepagePreference(context, classLoader) ?: return
                    entry.call("setKey", SettingsHostDefinitions.HOME_ENTRY_KEY)
                    entry.call("setTitle", SettingsHostDefinitions.MODULE_TITLE)
                    entry.call("setOrder", HOME_ENTRY_ORDER)
                    moduleIcon(context)?.let { entry.call("setIcon", it) }
                    entry.call("setOnPreferenceClickListener", createHomeEntryClickListener(classLoader, context, fragment, null))

                    val category = screen.call("findPreference", KEY_SYSTEM_CATEGORY)
                    val inserted = (category ?: screen).call("addPreference", entry) as? Boolean ?: false
                    if (inserted) {
                        adjustEntryAfterGeneral(category ?: screen, entry)
                    }
                    Log.i(TAG, "Injected ZUXOS+ entry into Settings homepage")
                }
            },
        )
    }

    private fun hookHostSettingsPage(classLoader: ClassLoader?) {
        val clazz = XposedHelpers.findClassIfExists(DASHBOARD_FRAGMENT, classLoader) ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreatePreferences",
            Bundle::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val fragment = param.thisObject ?: return
                    if (fragment.javaClass.name != SYSTEM_DASHBOARD_FRAGMENT) return
                    val args = fragment.call("getArguments") as? Bundle ?: return
                    if (!args.getBoolean(SettingsHostDefinitions.HOST_PAGE_MARKER, false)) return

                    val context = fragment.call("getContext") as? Context ?: return
                    rebuildHostSettingsPage(fragment, context, classLoader, args)
                    Log.i(TAG, "Rendered ZUXOS+ host settings page")
                }
            },
        )
    }

    private fun rebuildHostSettingsPage(fragment: Any, context: Context, classLoader: ClassLoader?, args: Bundle) {
        val preferenceManager = fragment.call("getPreferenceManager") ?: return
        val screen = preferenceManager.call("createPreferenceScreen", context) ?: return
        screen.call("setKey", "zuxos_plus_settings_screen")
        screen.call("setOrderingAsAdded", false)

        val pageId = args.getString(SettingsHostDefinitions.HOST_PAGE_ID, SettingsHostDefinitions.ROOT_PAGE_ID)
        if (pageId == SettingsHostDefinitions.ROOT_PAGE_ID) {
            renderRootPage(screen, context, classLoader, fragment)
        } else {
            SettingsHostDefinitions.page(pageId)?.let { page ->
                renderDetailPage(screen, context, classLoader, page)
            }
        }

        fragment.call("setPreferenceScreen", screen)
    }

    private fun renderRootPage(screen: Any, context: Context, classLoader: ClassLoader?, fragment: Any) {
        SettingsHostDefinitions.pages.forEachIndexed { index, page ->
            val preference = createArrowPreference(context, classLoader) ?: return@forEachIndexed
            preference.call("setKey", "zuxos_page_${page.id}")
            preference.call("setTitle", page.title)
            preference.call("setOrder", index)
            preference.call("setOnPreferenceClickListener", createHomeEntryClickListener(classLoader, context, fragment, page.id))
            screen.call("addPreference", preference)
        }
    }

    private fun renderDetailPage(screen: Any, context: Context, classLoader: ClassLoader?, page: SettingsHostPage) {
        page.items.forEachIndexed { index, item ->
            val preference = when (item) {
                is SettingsHostBooleanItem -> createBooleanPreference(context, classLoader, item)
                is SettingsHostTextItem -> createTextPreference(context, classLoader, item)
                is SettingsHostIntItem -> createIntPreference(context, classLoader, item)
                is SettingsHostFloatItem -> createFloatPreference(context, classLoader, item)
                is SettingsHostColorItem -> createColorPreference(context, classLoader, item)
            } ?: return@forEachIndexed
            preference.call("setOrder", index)
            screen.call("addPreference", preference)
        }
    }

    private fun createHomepagePreference(context: Context, classLoader: ClassLoader?): Any? =
        createInstance(
            classLoader,
            listOf(
                "com.android.settings.widget.HomepagePreference",
                "androidx.preference.Preference",
            ),
            context,
        )

    private fun createPreferenceCategory(context: Context, classLoader: ClassLoader?): Any? =
        createInstance(
            classLoader,
            listOf(
                "zui.appcompat.preference.PreferenceCategory",
                "androidx.preference.PreferenceCategory",
            ),
            context,
        )

    private fun createArrowPreference(context: Context, classLoader: ClassLoader?): Any? =
        createInstance(
            classLoader,
            listOf(
                "zui.appcompat.preference.PreferenceWithArrow",
                "zui.appcompat.preference.Preference",
                "androidx.preference.Preference",
            ),
            context,
        )

    private fun createSwitchPreference(context: Context, classLoader: ClassLoader?): Any? =
        createInstance(
            classLoader,
            listOf(
                "zui.appcompat.preference.SwitchPreference",
                "androidx.preference.SwitchPreferenceCompat",
                "androidx.preference.SwitchPreference",
            ),
            context,
        )

    private fun createEditTextPreference(context: Context, classLoader: ClassLoader?): Any? =
        createInstance(
            classLoader,
            listOf(
                "zui.appcompat.preference.EditTextPreference",
                "androidx.preference.EditTextPreference",
            ),
            context,
        )

    private fun createBooleanPreference(context: Context, classLoader: ClassLoader?, item: SettingsHostBooleanItem): Any? {
        val preference = createSwitchPreference(context, classLoader) ?: return null
        preference.call("setKey", item.key)
        preference.call("setTitle", item.title)
        if (item.summary.isNotBlank()) preference.call("setSummary", item.summary)
        preference.call("setPersistent", false)
        preference.call("setChecked", loadBoolean(context, item.key, item.defaultValue))
        preference.call("setOnPreferenceChangeListener", createBooleanChangeListener(classLoader, context, item))
        return preference
    }

    private fun createTextPreference(context: Context, classLoader: ClassLoader?, item: SettingsHostTextItem): Any? {
        val preference = createEditTextPreference(context, classLoader) ?: return null
        val value = loadString(context, item.key, item.defaultValue)
        preference.call("setKey", item.key)
        preference.call("setTitle", item.title)
        preference.call("setSummary", value.ifBlank { item.summary })
        preference.call("setPersistent", false)
        preference.call("setText", value)
        preference.call("setOnPreferenceChangeListener", createTextChangeListener(classLoader, context, item))
        return preference
    }

    private fun createIntPreference(context: Context, classLoader: ClassLoader?, item: SettingsHostIntItem): Any? {
        val preference = createEditTextPreference(context, classLoader) ?: return null
        val value = loadInt(context, item.key, item.defaultValue).coerceIn(item.min, item.max)
        val formatted = value.toString()
        preference.call("setKey", item.key)
        preference.call("setTitle", item.title)
        preference.call("setSummary", item.summary.ifBlank { formatted })
        preference.call("setPersistent", false)
        preference.call("setText", formatted)
        preference.call("setOnPreferenceChangeListener", createIntChangeListener(classLoader, context, item))
        return preference
    }

    private fun createFloatPreference(context: Context, classLoader: ClassLoader?, item: SettingsHostFloatItem): Any? {
        val preference = createEditTextPreference(context, classLoader) ?: return null
        val value = loadFloat(context, item.key, item.defaultValue).coerceIn(item.min, item.max)
        preference.call("setKey", item.key)
        preference.call("setTitle", item.title)
        preference.call("setSummary", formatFloat(value))
        preference.call("setPersistent", false)
        preference.call("setText", formatFloat(value))
        preference.call("setOnPreferenceChangeListener", createFloatChangeListener(classLoader, context, item))
        return preference
    }

    private fun createColorPreference(context: Context, classLoader: ClassLoader?, item: SettingsHostColorItem): Any? {
        val preference = createEditTextPreference(context, classLoader) ?: return null
        val value = loadInt(context, item.key, item.defaultValue)
        val hex = formatColor(value)
        preference.call("setKey", item.key)
        preference.call("setTitle", item.title)
        preference.call("setSummary", hex)
        preference.call("setPersistent", false)
        preference.call("setText", hex)
        preference.call("setOnPreferenceChangeListener", createColorChangeListener(classLoader, context, item))
        return preference
    }

    private fun createInstance(classLoader: ClassLoader?, classNames: List<String>, context: Context): Any? {
        classNames.forEach { className ->
            val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            runCatching {
                return clazz.getConstructor(Context::class.java).newInstance(context)
            }.onFailure {
                Log.w(TAG, "Cannot create $className", it)
            }
        }
        return null
    }

    private fun createHomeEntryClickListener(classLoader: ClassLoader?, context: Context, fragment: Any, pageId: String?): Any? {
        val listenerClass = XposedHelpers.findClassIfExists(
            "androidx.preference.Preference\$OnPreferenceClickListener",
            classLoader,
        ) ?: return null
        return Proxy.newProxyInstance(classLoader, arrayOf(listenerClass)) { _, method, args ->
            if (method.name != "onPreferenceClick") return@newProxyInstance false
            context.startActivity(createHostPageIntent(context, fragment, pageId))
            true
        }
    }

    private fun createBooleanChangeListener(
        classLoader: ClassLoader?,
        context: Context,
        item: SettingsHostBooleanItem,
    ): Any? {
        val listenerClass = XposedHelpers.findClassIfExists(
            "androidx.preference.Preference\$OnPreferenceChangeListener",
            classLoader,
        ) ?: return null
        return Proxy.newProxyInstance(classLoader, arrayOf(listenerClass)) { _, method, args ->
            if (method.name != "onPreferenceChange") return@newProxyInstance true
            val enabled = args?.getOrNull(1) as? Boolean ?: return@newProxyInstance false
            saveBoolean(context, item.key, enabled)
            true
        }
    }

    private fun createTextChangeListener(classLoader: ClassLoader?, context: Context, item: SettingsHostTextItem): Any? =
        createChangeListener(classLoader) { preference, value ->
            val text = value as? String ?: return@createChangeListener false
            saveString(context, item.key, text)
            preference?.call("setSummary", text.ifBlank { item.summary })
            true
        }

    private fun createFloatChangeListener(classLoader: ClassLoader?, context: Context, item: SettingsHostFloatItem): Any? =
        createChangeListener(classLoader) { preference, value ->
            val parsed = (value as? String)?.toFloatOrNull()?.coerceIn(item.min, item.max)
                ?: return@createChangeListener false
            val formatted = formatFloat(parsed)
            saveFloat(context, item.key, parsed)
            preference?.call("setSummary", formatted)
            preference?.call("setText", formatted)
            true
        }

    private fun createIntChangeListener(classLoader: ClassLoader?, context: Context, item: SettingsHostIntItem): Any? =
        createChangeListener(classLoader) { preference, value ->
            val parsed = (value as? String)?.toIntOrNull()?.coerceIn(item.min, item.max)
                ?: return@createChangeListener false
            val formatted = parsed.toString()
            saveInt(context, item.key, parsed)
            preference?.call("setSummary", item.summary.ifBlank { formatted })
            preference?.call("setText", formatted)
            true
        }

    private fun createColorChangeListener(classLoader: ClassLoader?, context: Context, item: SettingsHostColorItem): Any? =
        createChangeListener(classLoader) { preference, value ->
            val parsed = parseColor(value as? String) ?: return@createChangeListener false
            val formatted = formatColor(parsed)
            saveInt(context, item.key, parsed)
            preference?.call("setSummary", formatted)
            preference?.call("setText", formatted)
            true
        }

    private fun createChangeListener(
        classLoader: ClassLoader?,
        onChange: (preference: Any?, value: Any?) -> Boolean,
    ): Any? {
        val listenerClass = XposedHelpers.findClassIfExists(
            "androidx.preference.Preference\$OnPreferenceChangeListener",
            classLoader,
        ) ?: return null
        return Proxy.newProxyInstance(classLoader, arrayOf(listenerClass)) { _, method, args ->
            if (method.name != "onPreferenceChange") return@newProxyInstance true
            onChange(args?.getOrNull(0), args?.getOrNull(1))
        }
    }

    private fun loadBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        val extras = Bundle().apply {
            putBoolean(ModuleConfigProvider.EXTRA_DEFAULT_VALUE, defaultValue)
        }
        return runCatching {
            context.contentResolver
                .call(ModuleConfigProvider.URI, ModuleConfigProvider.METHOD_GET_BOOLEAN, key, extras)
                ?.getBoolean(ModuleConfigProvider.EXTRA_VALUE, defaultValue)
        }.getOrNull() ?: ModuleConfig.loadBoolean(context, key, defaultValue)
    }

    private fun saveBoolean(context: Context, key: String, value: Boolean) {
        val extras = Bundle().apply {
            putBoolean(ModuleConfigProvider.EXTRA_VALUE, value)
        }
        val written = runCatching {
            context.contentResolver.call(
                ModuleConfigProvider.URI,
                ModuleConfigProvider.METHOD_SET_BOOLEAN,
                key,
                extras,
            )
        }.isSuccess
        if (!written) {
            ModuleConfig.saveBoolean(context, key, value)
        }
    }

    private fun loadString(context: Context, key: String, defaultValue: String): String {
        val extras = Bundle().apply { putString(ModuleConfigProvider.EXTRA_DEFAULT_VALUE, defaultValue) }
        return runCatching {
            context.contentResolver
                .call(ModuleConfigProvider.URI, ModuleConfigProvider.METHOD_GET_STRING, key, extras)
                ?.getString(ModuleConfigProvider.EXTRA_VALUE)
        }.getOrNull() ?: ModuleConfig.loadString(context, key, defaultValue)
    }

    private fun saveString(context: Context, key: String, value: String) {
        val extras = Bundle().apply { putString(ModuleConfigProvider.EXTRA_VALUE, value) }
        val written = runCatching {
            context.contentResolver.call(ModuleConfigProvider.URI, ModuleConfigProvider.METHOD_SET_STRING, key, extras)
        }.isSuccess
        if (!written) ModuleConfig.saveString(context, key, value)
    }

    private fun loadFloat(context: Context, key: String, defaultValue: Float): Float {
        val extras = Bundle().apply { putFloat(ModuleConfigProvider.EXTRA_DEFAULT_VALUE, defaultValue) }
        return runCatching {
            context.contentResolver
                .call(ModuleConfigProvider.URI, ModuleConfigProvider.METHOD_GET_FLOAT, key, extras)
                ?.getFloat(ModuleConfigProvider.EXTRA_VALUE, defaultValue)
        }.getOrNull() ?: ModuleConfig.loadFloat(context, key, defaultValue)
    }

    private fun saveFloat(context: Context, key: String, value: Float) {
        val extras = Bundle().apply { putFloat(ModuleConfigProvider.EXTRA_VALUE, value) }
        val written = runCatching {
            context.contentResolver.call(ModuleConfigProvider.URI, ModuleConfigProvider.METHOD_SET_FLOAT, key, extras)
        }.isSuccess
        if (!written) ModuleConfig.saveFloat(context, key, value)
    }

    private fun loadInt(context: Context, key: String, defaultValue: Int): Int {
        val extras = Bundle().apply { putInt(ModuleConfigProvider.EXTRA_DEFAULT_VALUE, defaultValue) }
        return runCatching {
            context.contentResolver
                .call(ModuleConfigProvider.URI, ModuleConfigProvider.METHOD_GET_INT, key, extras)
                ?.getInt(ModuleConfigProvider.EXTRA_VALUE, defaultValue)
        }.getOrNull() ?: ModuleConfig.loadInt(context, key, defaultValue)
    }

    private fun saveInt(context: Context, key: String, value: Int) {
        val extras = Bundle().apply { putInt(ModuleConfigProvider.EXTRA_VALUE, value) }
        val written = runCatching {
            context.contentResolver.call(ModuleConfigProvider.URI, ModuleConfigProvider.METHOD_SET_INT, key, extras)
        }.isSuccess
        if (!written) ModuleConfig.saveInt(context, key, value)
    }

    private fun createHostPageIntent(context: Context, fragment: Any, pageId: String?): Intent {
        val args = Bundle().apply {
            putBoolean(SettingsHostDefinitions.HOST_PAGE_MARKER, true)
            putString(SettingsHostDefinitions.HOST_PAGE_ID, pageId ?: SettingsHostDefinitions.ROOT_PAGE_ID)
        }
        val metricsCategory = runCatching {
            fragment.call("getMetricsCategory") as? Int
        }.getOrNull() ?: 0
        val title = pageId?.let { SettingsHostDefinitions.page(it)?.title } ?: SettingsHostDefinitions.MODULE_TITLE
        return Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(PACKAGE_SETTINGS, SUB_SETTINGS)
            putExtra(EXTRA_SHOW_FRAGMENT, SYSTEM_DASHBOARD_FRAGMENT)
            putExtra(EXTRA_SHOW_FRAGMENT_ARGS, args)
            putExtra(EXTRA_SHOW_FRAGMENT_TITLE, title)
            putExtra(EXTRA_SOURCE_METRICS, metricsCategory)
            putExtra(EXTRA_IS_FROM_SETTINGS_HOMEPAGE, true)
        }
    }

    private fun moduleIcon(context: Context): Drawable? =
        runCatching { context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID) }.getOrNull()

    private fun adjustEntryAfterGeneral(group: Any, entry: Any) {
        val general = group.call("findPreference", KEY_GENERAL) ?: return
        val generalOrder = general.call("getOrder") as? Int ?: return
        entry.call("setOrder", generalOrder + 1)
    }

    private fun Any.call(methodName: String, vararg args: Any?): Any? =
        runCatching { XposedHelpers.callMethod(this, methodName, *args) }
            .onFailure { Log.w(TAG, "Call failed: ${this.javaClass.name}.$methodName", it) }
            .getOrNull()

    private fun formatFloat(value: Float): String =
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

    private fun formatColor(value: Int): String =
        String.format(Locale.US, "#%08X", value)

    private fun parseColor(value: String?): Int? {
        val raw = value?.trim()?.removePrefix("#") ?: return null
        val normalized = when (raw.length) {
            6 -> "FF$raw"
            8 -> raw
            else -> return null
        }
        return normalized.toLongOrNull(16)?.toInt()
    }
}
