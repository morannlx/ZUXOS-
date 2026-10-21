package me.inkdye.zuxos.hook.lenovo

import android.content.Context
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import java.io.File
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import me.inkdye.zuxos.hook.findHostClass
import me.inkdye.zuxos.hook.hookFirstMethod

object LenovoGameAssistantFeatureUnlockHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN
    override val targetPackage = PACKAGE_GAME_SERVICE
    override val targetPackages = setOf(PACKAGE_GAME_SERVICE, PACKAGE_SETTINGS, PACKAGE_GAME_SETTINGS)
    override val moduleName = "lenovo_game_assistant"
    override val alwaysRegister = true

    private const val PACKAGE_GAME_SERVICE = "com.zui.game.service"
    private const val PACKAGE_SETTINGS = "com.android.settings"
    private const val PACKAGE_GAME_SETTINGS = "com.zui.ugame.gamesetting"

    private data class FeatureGate(
        val className: String,
        val prefKey: String,
    )

    private val FEATURE_GATES = listOf(
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_VOICE_ASSISTANT",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_BARRAGE",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_BARRAGE,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_HILIGHT",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_ISLAND",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_ISLAND,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_PREVENT_MISOPERATION",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_NOTIFICATION_UNDISTURB",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_INTELLIGENT_PRE_DOWNLOAD",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_INTELLIGENT_BYPASS_CHARGING",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_MOBILE_NETWORK_ACCELERATE",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE,
        ),
        FeatureGate(
            "com.zui.ugame.gamesetting.feature.FEATURE_MAIN_SIM_ACCELERATE",
            ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE,
        ),
    )

    private val ELDEN_TRUE_SETTINGS_GETTERS = listOf(
        "isEldenPrc",
        "getIsLegionDevice",
        "getIsOneStepDevice",
        "isPad",
        "getIsLargeScreenDevice",
        "getSupportWideVision",
        "getSupportSemiRealVibration",
        "getShowSemiRealVibration",
        "getSupportClickVibration",
    )

    private val ELDEN_FALSE_SETTINGS_GETTERS = listOf(
        "isEldenRow",
        "isTopaz",
    )

    private val TEMPERATURE_FILE_PATHS = listOf(
        "/sys/class/thermal/thermal_zone9/temp",
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/class/thermal/thermal_zone1/temp",
        "/sys/class/thermal/thermal_zone2/temp",
        "/sys/devices/virtual/thermal/thermal_zone9/temp",
        "/sys/class/hwmon/hwmon0/temp1_input",
    )

    override fun PackageParam.onHook() {
        hookFeatureIsEnable()
        hookRomFeatureOpenGate()
        hookRepositorySwitchStatusGate()
        hookDeviceModelGates()
        hookSettingsModelGates()
        hookSystemProperties()
        hookRomFeatures()
        hookHardwareDisplayFixes()
    }

    private fun PackageParam.hookFeatureIsEnable() {
        FEATURE_GATES.forEach { gate ->
            runCatching {
                hookFirstMethod(gate.className, "isEnable", Context::class.java) {
                    before {
                        if (isEnabled(gate.prefKey)) result = true
                    }
                }
            }
        }
    }

    private fun PackageParam.hookRomFeatureOpenGate() {
        runCatching {
            hookFirstMethod("com.zui.game.service.RomFeatures", "isFeatureOpen", String::class.java) {
                before {
                    val key = args.firstOrNull() as? String ?: return@before
                    if (isFeatureForcedOpen(key)) result = true
                }
            }
        }.onFailure { YLog.error("$moduleName: hook RomFeatures.isFeatureOpen failed", it) }
    }

    private fun PackageParam.hookRepositorySwitchStatusGate() {
        runCatching {
            hookFirstMethod("com.zui.game.service.data.RepositoryImpl", "getSwitchStatus", String::class.java) {
                before {
                    val key = args.firstOrNull() as? String ?: return@before
                    if (isFeatureForcedOpen(key) && !hasRomFeatureKey(key)) result = FEATURE_STATUS_ON
                }
            }
        }.onFailure { YLog.error("$moduleName: hook RepositoryImpl.getSwitchStatus failed", it) }
    }

    private fun PackageParam.hookDeviceModelGates() {
        runCatching {
            findHostClass("com.zui.util.DeviceUtils")?.let { target ->
                hookBooleanGetter(target, "isElden", true, ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN)
                hookBooleanGetter(target, "isEldenRow", false, ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN)
                hookBooleanGetter(target, "isTOPAZ", false, ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN)
                hookBooleanGetter(target, "isPad", true) { anyDirectFeatureEnabled() || isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN) }
                hookBooleanGetter(target, "isRow", false) { isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN) }
            }
        }
    }

    private fun PackageParam.hookSettingsModelGates() {
        runCatching {
            findHostClass("com.zui.game.service.p031di.Settings")?.let { target ->
                ELDEN_TRUE_SETTINGS_GETTERS.forEach { methodName ->
                    hookBooleanGetter(target, methodName, true, ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN)
                }
                ELDEN_FALSE_SETTINGS_GETTERS.forEach { methodName ->
                    hookBooleanGetter(target, methodName, false, ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN)
                }
                listOf("getIsLegionDevice", "isPad", "getIsLargeScreenDevice").forEach { methodName ->
                    hookBooleanGetter(target, methodName, true) { anyDirectFeatureEnabled() }
                }
            }
        }
    }

    private fun PackageParam.hookSystemProperties() {
        runCatching {
            hookFirstMethod("com.zui.util.SystemPropertyReflect", "get", String::class.java) {
                before {
                    val key = args.firstOrNull() as? String ?: return@before
                    when (key) {
                        "ro.zui.software.llm_mode" -> {
                            if (isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE)) {
                                result = "TRUE"
                            }
                        }
                        "ro.zui.software.main_sub_aclr" -> {
                            if (isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE)) {
                                result = "TRUE"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun PackageParam.hookRomFeatures() {
        runCatching {
            val featuresClass = findHostClass("com.zui.game.service.FeaturesBaseOnRomKt") ?: return@runCatching
            hookFirstMethod(featuresClass, "getRomFeatures") {
                before {
                    if (!isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN)) return@before
                    result = featuresClass.getDeclaredMethod("getELDEN_FEATURES").invoke(null)
                }
            }
        }.onFailure { YLog.error("$moduleName: hook FeaturesBaseOnRomKt.getRomFeatures failed", it) }
    }

    private fun PackageParam.hookHardwareDisplayFixes() {
        if (packageName != PACKAGE_GAME_SERVICE) return
        runCatching {
            findHostClass("com.zui.game.service.util.HWDataInterface")?.let { target ->
                hookIntGetter(target, "getCpuCurFreq") { getLastCpuCoreCurrentFreq() }
                hookIntGetter(target, "getCpuCurFreq", Int::class.javaPrimitiveType!!) { getLastCpuCoreCurrentFreq() }
                hookIntGetter(target, "getCpuMaxFreq") { getLastCpuCoreMaxFreq() }
                hookIntGetter(target, "getTemp") { readTemperatureFromFile() }
                hookIntGetter(target, "getThermalTemp", Int::class.javaPrimitiveType!!) { readTemperatureFromFile() }
            }
        }.onFailure { YLog.error("$moduleName: hook HWDataInterface display fixes failed", it) }
    }

    private fun PackageParam.hookIntGetter(
        targetClass: Class<*>,
        methodName: String,
        vararg parameterTypes: Class<*>,
        valueProvider: () -> Int,
    ) {
        runCatching {
            hookFirstMethod(targetClass, methodName, *parameterTypes) {
                before {
                    if (!isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_SPOOF_ELDEN)) return@before
                    val value = valueProvider()
                    if (value > 0 || methodName.startsWith("getCpu")) result = value
                }
            }
        }
    }

    private fun PackageParam.hookBooleanGetter(
        targetClass: Class<*>,
        methodName: String,
        forcedValue: Boolean,
        prefKey: String,
    ) {
        hookBooleanGetter(targetClass, methodName, forcedValue) { isEnabled(prefKey) }
    }

    private fun PackageParam.hookBooleanGetter(
        targetClass: Class<*>,
        methodName: String,
        forcedValue: Boolean,
        shouldForce: () -> Boolean,
    ) {
        runCatching {
            hookFirstMethod(targetClass, methodName) {
                before {
                    if (shouldForce()) result = forcedValue
                }
            }
        }
    }

    private fun anyDirectFeatureEnabled(): Boolean {
        return FEATURE_GATES.any { isEnabled(it.prefKey) }
    }

    private fun getLastCpuCoreCurrentFreq(): Int {
        val lastCoreIndex = getLastCpuCoreIndex()
        if (lastCoreIndex >= 0) {
            readIntFile("/sys/devices/system/cpu/cpu$lastCoreIndex/cpufreq/scaling_cur_freq")?.let { return it }
        }
        return readIntFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            ?: readIntFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq")
            ?: 0
    }

    private fun getLastCpuCoreMaxFreq(): Int {
        val lastCoreIndex = getLastCpuCoreIndex()
        if (lastCoreIndex >= 0) {
            readIntFile("/sys/devices/system/cpu/cpu$lastCoreIndex/cpufreq/scaling_max_freq")?.let { return it }
        }
        return readIntFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq")
            ?: readIntFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            ?: 0
    }

    private fun getLastCpuCoreIndex(): Int {
        return runCatching {
            File("/sys/devices/system/cpu/")
                .listFiles { file -> file.name.matches(Regex("cpu\\d+")) }
                ?.mapNotNull { file -> file.name.removePrefix("cpu").toIntOrNull() }
                ?.maxOrNull()
                ?: -1
        }.getOrDefault(-1)
    }

    private fun readTemperatureFromFile(): Int {
        return TEMPERATURE_FILE_PATHS.firstNotNullOfOrNull { path ->
            readIntFile(path)?.takeIf { it > 0 }
        } ?: -1
    }

    private fun readIntFile(path: String): Int? {
        return runCatching {
            val file = File(path)
            if (!file.exists() || !file.canRead()) return null
            file.readLines().firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()
        }.getOrNull()
    }

    private fun PackageParam.isFeatureForcedOpen(key: String): Boolean {
        return when (key) {
            "key_voice_changer" -> isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_VOICE_ASSISTANT)
            "key_barrage" -> isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_BARRAGE)
            "option_item_highlight" -> isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_HIGHLIGHT)
            "key_item_island" -> isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_ISLAND)
            "key_block_mistake_touch", "option_item_prevent_misoperation" ->
                isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_PREVENT_MISOPERATION)
            "option_item_notification_undisturb", "key_block_notification" ->
                isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_NOTIFICATION_DND)
            "option_intelligent_pre_download", "key_intelligent_pre_download" ->
                isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_PRE_DOWNLOAD)
            "intelligent_bypass_charging", "key_bypass_charging" ->
                isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_INTELLIGENT_BYPASS_CHARGING)
            "option_item_network_boost", "key_mobile_data" ->
                isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MOBILE_NETWORK_ACCELERATE)
            "key_main_sim_accelerate", "option_item_main_sim_accelerate" ->
                isEnabled(ModuleConfig.LENOVO_GAME_ASSISTANT_FORCE_MAIN_SIM_ACCELERATE)
            else -> false
        }
    }

    private fun isEnabled(prefKey: String): Boolean {
        return ModuleConfig.isEnabled(prefKey, false)
    }

    private fun PackageParam.hasRomFeatureKey(key: String): Boolean {
        return runCatching {
            val featuresClass = findHostClass("com.zui.game.service.FeaturesBaseOnRomKt") ?: return@runCatching false
            val romFeatures = featuresClass.getDeclaredMethod("getRomFeatures").invoke(null)
            val keyList = romFeatures.javaClass.getMethod("getKeyList").invoke(romFeatures) as? Iterable<*>
            keyList?.any { featureKey ->
                featureKey?.javaClass?.getMethod("getKey")?.invoke(featureKey) == key
            } == true
        }.getOrDefault(false)
    }

    private const val FEATURE_STATUS_ON = 0
}
