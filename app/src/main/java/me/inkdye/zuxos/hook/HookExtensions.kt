package me.inkdye.zuxos.hook

import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

/**
 * YukiHook 1.5.x 移除了 1.3.x 的 `Class.hook { injectMember { ... } }` 写法。
 * 这里统一用宿主的 ClassLoader 解析成员，再交给新的 `Member.hook` API。
 */
typealias ClassicHooker = YukiHookCreator.ClassicMemberHooker

fun PackageParam.findHostClass(className: String): Class<*>? =
    XposedHelpers.findClassIfExists(className, hostClassLoader)

fun PackageParam.hookFirstMethod(
    clazz: Class<*>?,
    name: String,
    vararg parameterTypes: Class<*>,
    hooker: ClassicHooker.() -> Unit,
) {
    val method = clazz?.findMethod(name) { it.parameterTypes.contentEquals(parameterTypes) } ?: return
    method.hook(hooker = hooker)
}

fun PackageParam.hookFirstMethod(
    className: String,
    name: String,
    vararg parameterTypes: Class<*>,
    hooker: ClassicHooker.() -> Unit,
) = hookFirstMethod(findHostClass(className), name, *parameterTypes, hooker = hooker)

fun PackageParam.hookFirstMethodAny(
    clazz: Class<*>?,
    name: String,
    hooker: ClassicHooker.() -> Unit,
) {
    val method = clazz?.findMethod(name) { true } ?: return
    method.hook(hooker = hooker)
}

fun PackageParam.hookFirstMethodAny(
    className: String,
    name: String,
    hooker: ClassicHooker.() -> Unit,
) = hookFirstMethodAny(findHostClass(className), name, hooker)

fun PackageParam.hookFirstMethodByCount(
    clazz: Class<*>?,
    name: String,
    parameterCount: Int,
    hooker: ClassicHooker.() -> Unit,
) {
    val method = clazz?.findMethod(name) { it.parameterCount == parameterCount } ?: return
    method.hook(hooker = hooker)
}

fun PackageParam.hookFirstMethodByCount(
    className: String,
    name: String,
    parameterCount: Int,
    hooker: ClassicHooker.() -> Unit,
) = hookFirstMethodByCount(findHostClass(className), name, parameterCount, hooker)

fun PackageParam.hookFirstConstructor(
    clazz: Class<*>?,
    vararg parameterTypes: Class<*>,
    hooker: ClassicHooker.() -> Unit,
) {
    val constructor = clazz?.declaredConstructors
        ?.firstOrNull { it.parameterTypes.contentEquals(parameterTypes) }
        ?: return
    constructor.hook(hooker = hooker)
}

fun PackageParam.hookFirstConstructor(
    className: String,
    vararg parameterTypes: Class<*>,
    hooker: ClassicHooker.() -> Unit,
) = hookFirstConstructor(findHostClass(className), *parameterTypes, hooker = hooker)

fun PackageParam.hookAllConstructors(
    clazz: Class<*>?,
    hooker: ClassicHooker.() -> Unit,
) {
    clazz?.declaredConstructors?.forEach { it.hook(hooker = hooker) }
}

private fun Class<*>.findMethod(name: String, predicate: (Method) -> Boolean): Method? =
    declaredMethods.firstOrNull { it.name == name && predicate(it) }
        ?: methods.firstOrNull { it.name == name && predicate(it) }
