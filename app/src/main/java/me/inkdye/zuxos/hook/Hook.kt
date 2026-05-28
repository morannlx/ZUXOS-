package me.inkdye.zuxos.hook

import com.highcapable.yukihookapi.hook.param.PackageParam

/**
 * 所有 Hook 模块需实现的接口。
 *
 * 每个 Hook 必须定义：
 * - [prefKey]：用于存储启用状态的 SharedPreferences 键名
 * - [onHook]：实际的 Hook 逻辑，在 [PackageParam] 上下文中执行
 *
 * 可选：
 * - [defaultEnabled]：默认是否启用（默认为 false）
 * - [alwaysRegister]：是否跳过总开关检查并始终注册 Hook
 * - [targetPackage]：Hook 作用的目标应用包名，用于 UI 重启
 * - [targetPackages]：Hook 实际匹配的目标包集合，用于注册和日志过滤
 */
interface Hook {
    val prefKey: String
    val name: String get() = prefKey
    val moduleName: String get() = targetPackage ?: "module"
    val defaultEnabled: Boolean get() = false
    val alwaysRegister: Boolean get() = false
    val targetPackage: String? get() = null
    val targetPackages: Set<String> get() = targetPackage?.let { setOf(it) }.orEmpty()
    fun PackageParam.onHook()
}
