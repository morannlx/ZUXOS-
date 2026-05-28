package me.inkdye.zuxos.hook

/**
 * 全局 Hook 注册表。
 *
 * 所有 Hook 模块统一在此注册，[HookEntry] 遍历此列表完成条件加载。
 */
object Hooks : Iterable<Hook> {

    private val _all = linkedSetOf<Hook>()

    /** 所有已注册的 Hook（按注册顺序排列） */
    val all: List<Hook> get() = _all.toList()

    override fun iterator(): Iterator<Hook> = _all.iterator()

    fun register(vararg hooks: Hook) {
        hooks.forEach { hook -> _all.add(hook) }
    }
}
