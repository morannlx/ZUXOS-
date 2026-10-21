package me.inkdye.zuxos.hook.launcher

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import java.io.File
import java.util.Locale

object LauncherRecentsMemoryHook : Hook {
    override val prefKey = ModuleConfig.LAUNCHER_RECENTS_MEMORY
    override val targetPackage = LauncherConstants.PACKAGE_ZUX_LAUNCHER
    override val moduleName = "zux_launcher_recents_memory"

    private const val TAG = "ZUXOS.LauncherMemory"

    override fun PackageParam.onHook() {
        hookRecentsView("com.android.quickstep.views.LauncherRecentsView", hostClassLoader)
        hookRecentsView("com.android.quickstep.views.RecentsView", hostClassLoader)
        hookActivityResume("com.android.quickstep.RecentsActivity", hostClassLoader)
        hookActivityResume("com.zui.launcher.drawer.NormalLauncher", hostClassLoader)
        hookActivityResume("com.zui.launcher.drawer.DrawerLauncher", hostClassLoader)
        hookActivityResume("com.zui.launcher.drawer.LearningLauncher", hostClassLoader)
        hookActivityResume("com.android.launcher3.Launcher", hostClassLoader)
        hookLauncherStateChanges("com.android.launcher3.statemanager.StateManager", hostClassLoader)
    }

    private fun hookRecentsView(className: String, classLoader: ClassLoader?) {
        val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: return
        runHook("$className.onAttachedToWindow") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "onAttachedToWindow",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.onVisibilityChanged") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "onVisibilityChanged",
                View::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.onWindowVisibilityChanged") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "onWindowVisibilityChanged",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.setVisibility") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "setVisibility",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.applyLoadPlan") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "applyLoadPlan",
                MutableList::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.updateEmptyMessage") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "updateEmptyMessage",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.setContentAlpha") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "setContentAlpha",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.setInsets") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "setInsets",
                Rect::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        scheduleSync(param.thisObject as? View)
                    }
                },
            )
        }
        runHook("$className.onDetachedFromWindow") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "onDetachedFromWindow",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        removeMemoryView(param.thisObject as? View)
                    }
                },
            )
        }
    }

    private fun hookActivityResume(className: String, classLoader: ClassLoader?) {
        val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: return
        runHook("$className.onResume") {
            XposedHelpers.findAndHookMethod(
                clazz,
                "onResume",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as? Activity ?: return
                        scheduleSync(activity)
                    }
                },
            )
        }
    }

    private fun hookLauncherStateChanges(className: String, classLoader: ClassLoader?) {
        val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: return
        clazz.declaredMethods
            .filter { method ->
                method.name in setOf("goToState", "setRestState", "n") &&
                    method.parameterTypes.firstOrNull()?.name?.endsWith("BaseState") == true
            }
            .forEach { method ->
                runHook("$className.${method.name}/${method.parameterTypes.size}") {
                    XposedHelpers.findAndHookMethod(
                        clazz,
                        method.name,
                        *method.parameterTypes,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                handleStateStart(param.thisObject, param.args.firstOrNull())
                            }

                            override fun afterHookedMethod(param: MethodHookParam) {
                                handleStateStart(param.thisObject, param.args.firstOrNull())
                            }
                        },
                    )
                }
            }
        listOf("m", "onStateTransitionComplete").forEach { methodName ->
            runHook("$className.$methodName") {
                val method = clazz.declaredMethods.firstOrNull {
                    it.name == methodName && it.parameterTypes.firstOrNull()?.name?.endsWith("BaseState") == true
                } ?: return@runHook
                XposedHelpers.findAndHookMethod(
                    clazz,
                    method.name,
                    *method.parameterTypes,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            handleStateComplete(param.thisObject, param.args.firstOrNull())
                        }
                    },
                )
            }
        }
    }

    private fun handleStateStart(stateManager: Any?, state: Any?) {
        val activity = stateManager?.stateManagerActivity() ?: return
        if (state.isRecentsVisibleState()) {
            scheduleSync(activity)
        } else {
            hideMemoryView(activity)
        }
    }

    private fun handleStateComplete(stateManager: Any?, state: Any?) {
        val activity = stateManager?.stateManagerActivity() ?: return
        if (state.isRecentsVisibleState()) {
            scheduleSync(activity)
        } else {
            hideMemoryView(activity)
        }
    }

    private fun scheduleSync(activity: Activity?) {
        val decorView = activity?.window?.decorView ?: return
        decorView.post { syncMemoryView(activity) }
        decorView.postDelayed({ syncMemoryView(activity) }, 180L)
        decorView.postDelayed({ syncMemoryView(activity) }, 520L)
    }

    private fun scheduleSync(recentsView: View?) {
        recentsView ?: return
        recentsView.post { syncMemoryView(recentsView) }
        recentsView.postDelayed({ syncMemoryView(recentsView) }, 180L)
        recentsView.postDelayed({ syncMemoryView(recentsView) }, 520L)
    }

    private fun syncMemoryView(activity: Activity?) {
        val root = activity?.window?.decorView as? ViewGroup ?: return
        val recentsView = findRecentsView(root, activity) ?: return
        syncMemoryView(recentsView, root, activity)
    }

    private fun syncMemoryView(recentsView: View?) {
        val context = recentsView?.context ?: return
        val root = recentsView.rootView as? ViewGroup
        syncMemoryView(recentsView, root, context.findActivity())
    }

    private fun syncMemoryView(recentsView: View?, root: ViewGroup?, activity: Activity?) {
        val context = recentsView?.context ?: return
        val host = findOverlayHost(recentsView) ?: return
        if (root == null || !shouldShowMemory(recentsView, activity)) {
            hideMemoryView(host)
            return
        }
        val textView = host.findTaggedMemoryView() ?: createMemoryTextView(context).also { view ->
            host.addView(view, createMemoryLayoutParams(context))
        }
        textView.visibility = View.VISIBLE
        textView.bringToFront()
        updateMemoryText(textView, context)
        positionMemoryView(textView, host, recentsView)
        textView.postDelayed({ updateMemoryText(textView, context) }, 500L)
        textView.post { positionMemoryView(textView, host, recentsView) }
    }

    private fun createMemoryTextView(context: Context): TextView =
        TextView(context).apply {
            tag = LauncherConstants.MEMORY_VIEW_TAG
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.WHITE)
            includeFontPadding = false
            elevation = dp(context, 8).toFloat()
            maxLines = 1
            background = null
            setShadowLayer(dp(context, 2).toFloat(), 0f, dp(context, 1).toFloat(), Color.argb(130, 0, 0, 0))
            setPadding(0, 0, 0, 0)
            isClickable = false
            isFocusable = false
            updateMemoryText(this, context)
        }

    private fun createMemoryLayoutParams(context: Context): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END,
        ).apply {
            topMargin = dimensionPixelSize(context, "task_cleanup_top_margin", 39)
            rightMargin = fallbackMemoryRightMargin(context)
        }

    private fun removeMemoryView(recentsView: View?) {
        val host = recentsView?.let { findOverlayHost(it) } ?: return
        host.findTaggedMemoryView()?.let { host.removeView(it) }
    }

    private fun hideMemoryView(host: ViewGroup?) {
        host?.findTaggedMemoryView()?.visibility = View.GONE
    }

    private fun hideMemoryView(activity: Activity?) {
        val root = activity?.window?.decorView as? ViewGroup ?: return
        root.findTaggedMemoryViewRecursive()?.visibility = View.GONE
    }

    private fun findOverlayHost(recentsView: View): FrameLayout? {
        val activity = recentsView.context.findActivity()
        val dragLayer = activity?.callNoArgMethodOrNull("getDragLayer") as? FrameLayout
            ?: activity?.findViewByResourceName("drag_layer") as? FrameLayout
        if (dragLayer != null) return dragLayer
        val rootDragLayer = (recentsView.rootView as? ViewGroup)?.findViewByResourceName("drag_layer") as? FrameLayout
        if (rootDragLayer != null) return rootDragLayer
        val content = activity?.findViewById<FrameLayout>(android.R.id.content)
        if (content != null) return content
        var current: View? = recentsView.rootView
        if (current is FrameLayout) return current
        current = recentsView.parent as? View
        while (current != null) {
            if (current is FrameLayout) return current
            current = current.parent as? View
        }
        return null
    }

    private fun findRecentsView(root: ViewGroup, context: Context): View? {
        val overviewId = context.resources.getIdentifier(
            "overview_panel",
            "id",
            LauncherConstants.PACKAGE_ZUX_LAUNCHER,
        )
        if (overviewId != 0) {
            root.findViewById<View>(overviewId)?.let { return it }
        }
        return root.findFirstViewByClassName(
            "com.android.quickstep.views.LauncherRecentsView",
            "com.android.quickstep.views.RecentsView",
        )
    }

    private fun positionMemoryView(textView: TextView, host: FrameLayout, recentsView: View) {
        val anchor = recentsView.callNoArgMethodOrNull("getClearAllButton") as? View
        val layoutParams = textView.layoutParams as? FrameLayout.LayoutParams ?: return
        if (anchor == null || !anchor.canAnchorMemoryView() || host.width <= 0) {
            layoutParams.gravity = Gravity.TOP or Gravity.END
            layoutParams.topMargin = dimensionPixelSize(textView.context, "task_cleanup_top_margin", 39)
            layoutParams.rightMargin = fallbackMemoryRightMargin(textView.context)
            textView.layoutParams = layoutParams
            return
        }
        val clearAllButton = anchor

        val hostLocation = IntArray(2)
        val anchorLocation = IntArray(2)
        host.getLocationOnScreen(hostLocation)
        clearAllButton.getLocationOnScreen(anchorLocation)

        val anchorLeft = anchorLocation[0] - hostLocation[0]
        val anchorTop = anchorLocation[1] - hostLocation[1]
        val textHeight = textView.height.takeIf { it > 0 } ?: dp(textView.context, 18)
        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.topMargin = (anchorTop + (clearAllButton.height - textHeight) / 2).coerceAtLeast(0)
        layoutParams.rightMargin = (host.width - anchorLeft + dp(textView.context, 12)).coerceAtLeast(dp(textView.context, 8))
        textView.layoutParams = layoutParams
    }

    private fun shouldShowMemory(recentsView: View, activity: Activity?): Boolean {
        if (activity?.javaClass?.name == "com.android.quickstep.RecentsActivity") {
            return recentsView.hasVisibleFootprint() && activity.isInOverviewState()
        }
        if (activity?.isInOverviewShowingState() == true) return true
        return false
    }

    private fun Activity.isInOverviewState(): Boolean {
        val stateManager = callNoArgMethodOrNull("getStateManager")
            ?: objectFieldOrNull("mStateManager")
            ?: objectFieldOrNull("stateManager")
            ?: return false
        val states = listOfNotNull(
            stateManager.callNoArgMethodOrNull("getState"),
            stateManager.callNoArgMethodOrNull("getCurrentState"),
            stateManager.callNoArgMethodOrNull("getCurrentStableState"),
            stateManager.objectFieldOrNull("mState"),
            stateManager.objectFieldOrNull("state"),
        )
        return states.any { it.isRecentsVisibleState() }
    }

    private fun Activity.isInOverviewShowingState(): Boolean {
        val stateManager = callNoArgMethodOrNull("getStateManager")
            ?: objectFieldOrNull("mStateManager")
            ?: objectFieldOrNull("stateManager")
            ?: return false
        val current = stateManager.callNoArgMethodOrNull("getState")
            ?: stateManager.callNoArgMethodOrNull("getCurrentState")
            ?: stateManager.objectFieldOrNull("mState")
            ?: stateManager.objectFieldOrNull("state")
            ?: return false
        val target = stateManager.callNoArgMethodOrNull("getTargetState")
            ?: stateManager.objectFieldOrNull("f6648a")?.objectFieldOrNull("f6668c")
            ?: stateManager.objectFieldOrNull("mTargetState")
        if (target != null && !target.isRecentsVisibleState()) return false
        return current.isRecentsVisibleState()
    }

    private fun Any?.isRecentsVisibleState(): Boolean {
        val state = this ?: return false
        return (state.objectFieldOrNull("isRecentsViewVisible") as? Boolean) == true ||
            (state.callNoArgMethodOrNull("isRecentsViewVisible") as? Boolean) == true
    }

    private fun Any.stateManagerActivity(): Activity? {
        val container = objectFieldOrNull("f6650c")
            ?: objectFieldOrNull("mActivity")
            ?: objectFieldOrNull("mContainer")
            ?: objectFieldOrNull("container")
        return container as? Activity
            ?: container?.callNoArgMethodOrNull("asContext") as? Activity
            ?: container?.callNoArgMethodOrNull("getActivity") as? Activity
    }

    private fun ViewGroup.findFirstViewByClassName(vararg classNames: String): View? {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.javaClass.name in classNames) return child
            if (child is ViewGroup) {
                child.findFirstViewByClassName(*classNames)?.let { return it }
            }
        }
        return null
    }

    private fun Activity.findViewByResourceName(name: String): View? {
        val id = resources.getIdentifier(name, "id", LauncherConstants.PACKAGE_ZUX_LAUNCHER)
        return if (id != 0) findViewById(id) else null
    }

    private fun ViewGroup.findViewByResourceName(name: String): View? {
        val id = context.resources.getIdentifier(name, "id", LauncherConstants.PACKAGE_ZUX_LAUNCHER)
        return if (id != 0) findViewById(id) else null
    }

    private fun Context.findActivity(): Activity? {
        var current = this
        while (current is android.content.ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun ViewGroup.findTaggedMemoryView(): TextView? {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child is TextView && child.tag == LauncherConstants.MEMORY_VIEW_TAG) return child
        }
        return null
    }

    private fun ViewGroup.findTaggedMemoryViewRecursive(): TextView? {
        findTaggedMemoryView()?.let { return it }
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child is ViewGroup) {
                child.findTaggedMemoryViewRecursive()?.let { return it }
            }
        }
        return null
    }

    private fun View.hasVisibleFootprint(): Boolean {
        if (visibility != View.VISIBLE || windowVisibility != View.VISIBLE || alpha <= 0.01f) return false
        if (width <= 0 || height <= 0) return false
        val rect = Rect()
        return getGlobalVisibleRect(rect) && rect.width() > 0 && rect.height() > 0
    }

    private fun View?.canAnchorMemoryView(): Boolean {
        this ?: return false
        if (parent == null || visibility != View.VISIBLE || windowVisibility != View.VISIBLE || alpha <= 0.01f) return false
        if (width <= 0 || height <= 0) return false
        val rect = Rect()
        return getGlobalVisibleRect(rect) && rect.width() > 0 && rect.height() > 0
    }

    private fun View.contentAlphaOrOne(): Float =
        (callNoArgMethodOrNull("getContentAlpha") as? Number)?.toFloat()
            ?: (objectFieldOrNull("mContentAlpha") as? Number)?.toFloat()
            ?: 1f

    private fun Any.callNoArgMethodOrNull(name: String): Any? =
        runCatching { XposedHelpers.callMethod(this, name) }.getOrNull()

    private fun Any.objectFieldOrNull(name: String): Any? =
        runCatching { XposedHelpers.getObjectField(this, name) }.getOrNull()

    private fun updateMemoryText(textView: TextView?, context: Context) {
        textView ?: return
        val memory = readMemoryFromProc() ?: readMemoryFromActivityManager(context) ?: return
        textView.text = "${formatAvailableGb(memory.availableBytes)}可用 | ${formatTotalGb(memory.totalBytes)}"
    }

    private fun readMemoryFromProc(): MemorySnapshot? =
        runCatching {
            val values = File("/proc/meminfo").readLines().mapNotNull { line ->
                val parts = line.split(Regex("\\s+"))
                val key = parts.getOrNull(0)?.removeSuffix(":") ?: return@mapNotNull null
                val valueKb = parts.getOrNull(1)?.toLongOrNull() ?: return@mapNotNull null
                key to valueKb
            }.toMap()
            val totalKb = values["MemTotal"] ?: return@runCatching null
            val availableKb = values["MemAvailable"] ?: values["MemFree"] ?: return@runCatching null
            MemorySnapshot(
                totalBytes = totalKb * 1024L,
                availableBytes = availableKb * 1024L,
            )
        }.getOrNull()

    private fun readMemoryFromActivityManager(context: Context): MemorySnapshot? {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return MemorySnapshot(info.totalMem, info.availMem)
    }

    private fun formatAvailableGb(bytes: Long): String {
        val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
        return String.format(Locale.US, "%.2f GB", gb)
    }

    private fun formatTotalGb(bytes: Long): String {
        val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
        val roundedPhysical = kotlin.math.ceil(gb).toInt().coerceAtLeast(1)
        return "$roundedPhysical GB"
    }

    private fun dp(context: Context, value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics,
        ).toInt()

    private fun dimensionPixelSize(context: Context, name: String, fallbackDp: Int): Int {
        val id = context.resources.getIdentifier(name, "dimen", LauncherConstants.PACKAGE_ZUX_LAUNCHER)
        return if (id != 0) {
            runCatching { context.resources.getDimensionPixelSize(id) }.getOrDefault(dp(context, fallbackDp))
        } else {
            dp(context, fallbackDp)
        }
    }

    private fun fallbackMemoryRightMargin(context: Context): Int {
        return dimensionPixelSize(context, "overview_options_view_right_margin_min", 219)
    }

    private fun runHook(name: String, block: () -> Unit) {
        runCatching(block).onFailure { Log.w(TAG, "Hook failed: $name", it) }
    }

    private data class MemorySnapshot(
        val totalBytes: Long,
        val availableBytes: Long,
    ) {
        val usedBytes: Long get() = (totalBytes - availableBytes).coerceAtLeast(0L)
    }
}
