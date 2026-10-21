package me.inkdye.zuxos.hook.lenovo

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceControl
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object LenovoVivoStyleHangingHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_VIVO_STYLE_HANGING_ENABLED
    override val targetPackage = PACKAGE_SYSTEMUI
    override val moduleName = "lenovo_vivo_style_hanging"

    private const val TAG = "ZUXOS.VivoHanging"
    private const val PACKAGE_SYSTEMUI = "com.android.systemui"

    private const val SIDE_LEFT = 1
    private const val SIDE_RIGHT = 2
    private const val EDGE_DIM_DELAY_MS = 3_000L
    private const val EDGE_HALF_ALPHA = 0.78f
    private const val EDGE_DIM_ALPHA = 0.50f
    private const val TASK_OFFSCREEN_GAP_DP = 12

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val taskStates = mutableMapOf<Int, TaskEdgeState>()

    override fun PackageParam.onHook() {
        val classLoader = hostClassLoader
        hookHangingController(classLoader)
        hookWindowDecorationTouch(classLoader)
        hookWindowDecorViewModel(classLoader)
    }

    private fun hookHangingController(classLoader: ClassLoader?) {
        val controllerClass = XposedHelpers.findClassIfExists(
            "com.android.wm.shell.desktopmode.OvHangingModeController",
            classLoader,
        ) ?: run {
            Log.w(TAG, "OvHangingModeController not found")
            return
        }

        XposedHelpers.findAndHookMethod(
            controllerClass,
            "hangTask",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val taskId = param.args[0] as Int
                        val ctrlType = param.args[1] as Int
                        val enabled = param.args[2] as Boolean
                        val resultBounds = param.result as? Rect
                        if (enabled && resultBounds != null && !resultBounds.isEmpty) {
                            onEnterHanging(param.thisObject, taskId, ctrlType, resultBounds)
                        } else if (!enabled) {
                            clearTask(taskId, restoreAlpha = true, reason = "hangTask disabled")
                        }
                    }.onFailure { Log.w(TAG, "hangTask hook failed", it) }
                }
            },
        )

        XposedHelpers.findAndHookMethod(
            controllerClass,
            "onReposition",
            ActivityManager.RunningTaskInfo::class.java,
            PointF::class.java,
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val taskInfo = param.args[0] as? ActivityManager.RunningTaskInfo ?: return
                        val downPoint = param.args[1] as? PointF ?: return
                        val x = param.args[2] as? Float ?: return
                        val y = param.args[3] as? Float ?: return
                        if (handleRepositionGesture(param.thisObject, taskInfo, downPoint, x, y)) {
                            param.result = null
                        }
                    }.onFailure { Log.w(TAG, "onReposition hook failed", it) }
                }
            },
        )

        XposedHelpers.findAndHookMethod(
            controllerClass,
            "exitHangingMode",
            ActivityManager.RunningTaskInfo::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val taskInfo = param.args[0] as? ActivityManager.RunningTaskInfo ?: return
                        clearTask(taskInfo.taskId, restoreAlpha = true, reason = "exitHangingMode")
                    }.onFailure { Log.w(TAG, "exitHangingMode cleanup failed", it) }
                }
            },
        )

        Log.i(TAG, "Hooked OvHangingModeController")
    }

    private fun hookWindowDecorationTouch(classLoader: ClassLoader?) {
        val decorationClass = XposedHelpers.findClassIfExists(
            "com.android.wm.shell.windowdecor.DesktopModeWindowDecoration",
            classLoader,
        ) ?: run {
            Log.w(TAG, "DesktopModeWindowDecoration not found")
            return
        }

        XposedHelpers.findAndHookMethod(
            decorationClass,
            "checkTouchEvent",
            MotionEvent::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val event = param.args[0] as? MotionEvent ?: return
                        if (event.actionMasked != MotionEvent.ACTION_UP) return
                        val taskInfo = param.thisObject.fieldOrNull("mTaskInfo")
                            as? ActivityManager.RunningTaskInfo ?: return
                        val state = taskStates[taskInfo.taskId] ?: return
                        if (state.edgeState != EdgeState.EDGE_HALF && state.edgeState != EdgeState.EDGE_DIM) return
                        restoreToHanging(state)
                        param.result = null
                    }.onFailure { Log.w(TAG, "checkTouchEvent hook failed", it) }
                }
            },
        )

        XposedHelpers.findAndHookMethod(
            decorationClass,
            "relayout",
            ActivityManager.RunningTaskInfo::class.java,
            SurfaceControl.Transaction::class.java,
            SurfaceControl.Transaction::class.java,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            android.graphics.Region::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val taskInfo = param.args[0] as? ActivityManager.RunningTaskInfo ?: return
                        if (!taskInfo.booleanFieldOrFalse("isHangingMode") || !taskInfo.isVisible) {
                            clearTask(taskInfo.taskId, restoreAlpha = true, reason = "decoration relayout visible=${taskInfo.isVisible} hanging=${taskInfo.booleanFieldOrFalse("isHangingMode")}")
                        }
                    }.onFailure { Log.w(TAG, "relayout cleanup failed", it) }
                }
            },
        )

        Log.i(TAG, "Hooked DesktopModeWindowDecoration.checkTouchEvent")
    }

    private fun hookWindowDecorViewModel(classLoader: ClassLoader?) {
        val viewModelClass = XposedHelpers.findClassIfExists(
            "com.android.wm.shell.windowdecor.DesktopModeWindowDecorViewModel",
            classLoader,
        ) ?: run {
            Log.w(TAG, "DesktopModeWindowDecorViewModel not found")
            return
        }

        runCatching {
            XposedHelpers.findAndHookMethod(
                viewModelClass,
                "onTaskInfoChanged",
                ActivityManager.RunningTaskInfo::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val taskInfo = param.args[0] as? ActivityManager.RunningTaskInfo ?: return
                            reconcileTaskInfo(taskInfo, "onTaskInfoChanged")
                        }.onFailure { Log.w(TAG, "onTaskInfoChanged cleanup failed", it) }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "hook onTaskInfoChanged failed", it) }

        runCatching {
            XposedHelpers.findAndHookMethod(
                viewModelClass,
                "destroyWindowDecoration",
                ActivityManager.RunningTaskInfo::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val taskInfo = param.args[0] as? ActivityManager.RunningTaskInfo ?: return
                            clearTask(taskInfo.taskId, restoreAlpha = true, reason = "destroyWindowDecoration")
                        }.onFailure { Log.w(TAG, "destroyWindowDecoration cleanup failed", it) }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "hook destroyWindowDecoration failed", it) }

        runCatching {
            XposedHelpers.findAndHookMethod(
                viewModelClass,
                "onTaskVanished",
                ActivityManager.RunningTaskInfo::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val taskInfo = param.args[0] as? ActivityManager.RunningTaskInfo ?: return
                            clearTask(taskInfo.taskId, restoreAlpha = true, reason = "onTaskVanished")
                        }.onFailure { Log.w(TAG, "onTaskVanished cleanup failed", it) }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "hook onTaskVanished failed", it) }

        Log.i(TAG, "Hooked DesktopModeWindowDecorViewModel lifecycle")
    }

    private fun onEnterHanging(controller: Any, taskId: Int, ctrlType: Int, bounds: Rect) {
        val side = sideFromBounds(controller, bounds, ctrlType)
        val previous = taskStates[taskId]
        if (previous != null) {
            clearState(previous, restoreAlpha = true, reason = "replace hanging state")
            taskStates.remove(taskId)
        }
        val state = TaskEdgeState(
            taskId = taskId,
            controller = controller,
            edgeState = EdgeState.HANGING,
            side = side,
            hangingBounds = Rect(bounds),
            edgeBounds = Rect(),
            iconBounds = Rect(),
            taskInfo = getRunningTaskInfo(controller, taskId),
            taskSurface = findTaskSurface(controller, taskId),
        )
        taskStates[taskId] = state
        applyTaskAlpha(state, 1f)
        Log.i(TAG, "enter hanging task=$taskId side=${side.label()} bounds=$bounds ctrlType=$ctrlType")
    }

    private fun handleRepositionGesture(
        controller: Any,
        taskInfo: ActivityManager.RunningTaskInfo,
        downPoint: PointF,
        x: Float,
        y: Float,
    ): Boolean {
        val taskId = taskInfo.taskId
        val state = taskStates[taskId] ?: ensureHangingState(controller, taskInfo) ?: return false
        state.controller = controller
        state.taskInfo = taskInfo
        state.lastInteractionTime = System.currentTimeMillis()
        val dx = x - downPoint.x
        val dy = y - downPoint.y

        if ((state.edgeState == EdgeState.HANGING ||
                state.edgeState == EdgeState.EDGE_HALF ||
                state.edgeState == EdgeState.EDGE_DIM) &&
            shouldRestoreToNormal(dx, dy)
        ) {
            Log.i(TAG, "restore normal by drag down task=$taskId dx=$dx dy=$dy")
            restoreToNormal(state, taskInfo)
            return true
        }

        if (state.edgeState == EdgeState.HANGING && shouldEnterEdge(state, dx, dy, controller, taskId)) {
            val hangingBounds = getHangingBounds(controller, taskId) ?: state.hangingBounds
            val hiddenTaskBounds = calculateHiddenTaskBounds(controller, hangingBounds, state.side)
            val iconBounds = calculateIconBounds(controller, hangingBounds, state.side)
            Log.i(TAG, "enter edge half task=$taskId side=${state.side.label()} from=$hangingBounds to=$hiddenTaskBounds icon=$iconBounds dx=$dx dy=$dy")
            state.hangingBounds = Rect(hangingBounds)
            state.edgeBounds = Rect(hiddenTaskBounds)
            state.iconBounds = Rect(iconBounds)
            state.taskSurface = state.taskSurface ?: findTaskSurface(controller, taskId)
            applyTaskAlpha(state, 1f)
            if (!showEdgeIcon(state, EDGE_HALF_ALPHA)) {
                Log.w(TAG, "edge icon unavailable, keep native hanging task=$taskId")
                state.edgeBounds.setEmpty()
                state.iconBounds.setEmpty()
                return true
            }
            if (!reposition(controller, taskId, hangingBounds, hiddenTaskBounds, moved = true)) {
                Log.w(TAG, "hide task reposition failed, restore icon and keep native hanging task=$taskId")
                state.iconOverlay?.remove()
                state.iconOverlay = null
                state.edgeBounds.setEmpty()
                state.iconBounds.setEmpty()
                return true
            }
            state.edgeState = EdgeState.EDGE_HALF
            scheduleDim(state)
            scheduleEdgeWatch(state)
            return true
        }

        return false
    }

    private fun ensureHangingState(
        controller: Any,
        taskInfo: ActivityManager.RunningTaskInfo,
    ): TaskEdgeState? {
        if (!taskInfo.booleanFieldOrFalse("isHangingMode")) return null
        val bounds = getHangingBounds(controller, taskInfo.taskId) ?: return null
        val state = TaskEdgeState(
            taskId = taskInfo.taskId,
            controller = controller,
            edgeState = EdgeState.HANGING,
            side = sideFromBounds(controller, bounds, 0),
            hangingBounds = Rect(bounds),
            edgeBounds = Rect(),
            iconBounds = Rect(),
            taskInfo = taskInfo,
            taskSurface = findTaskSurface(controller, taskInfo.taskId),
        )
        taskStates[taskInfo.taskId] = state
        return state
    }

    private fun shouldEnterEdge(
        state: TaskEdgeState,
        dx: Float,
        dy: Float,
        controller: Any,
        taskId: Int,
    ): Boolean {
        val threshold = dp(controller, EDGE_PUSH_THRESHOLD_DP)
        val verticalLimit = dp(controller, 56)
        val pushedByGesture = when (state.side) {
            SIDE_LEFT -> dx <= -threshold
            else -> dx >= threshold
        } && abs(dy) <= verticalLimit

        val hangingBounds = getHangingBounds(controller, taskId) ?: state.hangingBounds
        val stableBounds = getStableBounds(controller)
        val pushedByBounds = stableBounds != null && when (state.side) {
            SIDE_LEFT -> hangingBounds.left <= stableBounds.left + dp(controller, 8).toInt()
            else -> hangingBounds.right >= stableBounds.right - dp(controller, 8).toInt()
        }
        return pushedByGesture || pushedByBounds
    }

    private fun shouldRestoreToNormal(dx: Float, dy: Float): Boolean {
        val downThreshold = 56f
        return dy >= downThreshold && abs(dx) <= max(96f, dy * 1.2f)
    }

    private fun calculateHiddenTaskBounds(controller: Any, hangingBounds: Rect, side: Int): Rect {
        val stableBounds = getStableBounds(controller) ?: Rect(0, 0, 0, 0)
        val gap = dp(controller, TASK_OFFSCREEN_GAP_DP).toInt()
        val topMin = if (stableBounds.isEmpty) hangingBounds.top else stableBounds.top + dp(controller, 8).toInt()
        val topMax = if (stableBounds.isEmpty) {
            hangingBounds.top
        } else {
            stableBounds.bottom - hangingBounds.height() - dp(controller, 8).toInt()
        }
        val top = if (topMax >= topMin) hangingBounds.top.coerceIn(topMin, topMax) else hangingBounds.top
        val left = if (side == SIDE_LEFT) {
            stableBounds.left - hangingBounds.width() - gap
        } else {
            stableBounds.right + gap
        }
        return Rect(left, top, left + hangingBounds.width(), top + hangingBounds.height())
    }

    private fun calculateIconBounds(controller: Any, hangingBounds: Rect, side: Int): Rect {
        val stableBounds = getStableBounds(controller) ?: Rect(0, 0, 0, 0)
        val iconSize = dp(controller, EDGE_ICON_SIZE_DP).toInt()
        val visibleWidth = max(
            dp(controller, EDGE_MIN_VISIBLE_DP).toInt(),
            min((iconSize * 0.52f).toInt(), dp(controller, EDGE_MAX_VISIBLE_DP).toInt()),
        )
        val topMin = if (stableBounds.isEmpty) 0 else stableBounds.top + dp(controller, 8).toInt()
        val topMax = if (stableBounds.isEmpty) {
            hangingBounds.centerY() - iconSize / 2
        } else {
            stableBounds.bottom - iconSize - dp(controller, 8).toInt()
        }
        val preferredTop = hangingBounds.centerY() - iconSize / 2
        val top = if (topMax >= topMin) preferredTop.coerceIn(topMin, topMax) else preferredTop
        val left = if (side == SIDE_LEFT) {
            stableBounds.left - iconSize + visibleWidth
        } else {
            stableBounds.right - visibleWidth
        }
        return Rect(left, top, left + iconSize, top + iconSize)
    }

    private fun restoreToHanging(state: TaskEdgeState) {
        val controller = state.controller ?: return
        cancelDim(state)
        cancelEdgeWatch(state)
        state.iconOverlay?.remove()
        state.iconOverlay = null
        applyTaskAlpha(state, 1f)
        val startBounds = if (!state.edgeBounds.isEmpty) state.edgeBounds else getHangingBounds(controller, state.taskId)
        val endBounds = state.hangingBounds
        if (startBounds != null && !endBounds.isEmpty) {
            Log.i(TAG, "restore edge to hanging task=${state.taskId} from=$startBounds to=$endBounds")
            reposition(controller, state.taskId, startBounds, endBounds, moved = true)
        }
        state.edgeState = EdgeState.HANGING
        state.edgeBounds.setEmpty()
        state.iconBounds.setEmpty()
    }

    private fun restoreToNormal(state: TaskEdgeState, fallbackTaskInfo: ActivityManager.RunningTaskInfo? = null) {
        val controller = state.controller ?: return
        val taskInfo = fallbackTaskInfo ?: state.taskInfo ?: getRunningTaskInfo(controller, state.taskId)
        cancelDim(state)
        cancelEdgeWatch(state)
        state.iconOverlay?.remove()
        state.iconOverlay = null
        applyTaskAlpha(state, 1f)
        if (taskInfo != null) {
            runCatching {
                XposedHelpers.callMethod(controller, "exitHangingMode", taskInfo)
            }.onFailure {
                Log.w(TAG, "exitHangingMode failed, fallback to hangTask(false)", it)
                XposedHelpers.callMethod(controller, "hangTask", state.taskId, 0, false, true)
            }
        } else {
            XposedHelpers.callMethod(controller, "hangTask", state.taskId, 0, false, true)
        }
        clearTask(state.taskId, restoreAlpha = false, reason = "restoreToNormal")
    }

    private fun scheduleDim(state: TaskEdgeState) {
        cancelDim(state)
        val runnable = Runnable {
            val current = taskStates[state.taskId] ?: return@Runnable
            if (current.edgeState == EdgeState.EDGE_HALF) {
                current.edgeState = EdgeState.EDGE_DIM
                current.iconOverlay?.setAlpha(EDGE_DIM_ALPHA)
                Log.i(TAG, "edge dim task=${current.taskId}")
            }
        }
        state.dimRunnable = runnable
        mainHandler.postDelayed(runnable, EDGE_DIM_DELAY_MS)
    }

    private fun cancelDim(state: TaskEdgeState) {
        state.dimRunnable?.let { mainHandler.removeCallbacks(it) }
        state.dimRunnable = null
    }

    private fun scheduleEdgeWatch(state: TaskEdgeState) {
        cancelEdgeWatch(state)
        val runnable = object : Runnable {
            override fun run() {
                val current = taskStates[state.taskId] ?: return
                if (current.edgeState != EdgeState.EDGE_HALF && current.edgeState != EdgeState.EDGE_DIM) return
                val controller = current.controller
                if (controller == null) {
                    clearTask(current.taskId, restoreAlpha = true, reason = "watch missing controller")
                    return
                }
                val taskInfo = getRunningTaskInfo(controller, current.taskId)
                val hangingBounds = getHangingBounds(controller, current.taskId)
                val stillEdge = taskInfo?.booleanFieldOrFalse("isHangingMode") == true &&
                    hangingBounds != null &&
                    rectNear(hangingBounds, current.edgeBounds, dp(controller, 12).toInt())
                if (!stillEdge) {
                    Log.i(
                        TAG,
                        "edge state stale, clear overlay task=${current.taskId} taskInfo=$taskInfo hanging=$hangingBounds edge=${current.edgeBounds}",
                    )
                    clearTask(current.taskId, restoreAlpha = true, reason = "watch stale edge")
                    return
                }
                mainHandler.postDelayed(this, EDGE_WATCH_INTERVAL_MS)
            }
        }
        state.watchRunnable = runnable
        mainHandler.postDelayed(runnable, EDGE_WATCH_INITIAL_DELAY_MS)
    }

    private fun cancelEdgeWatch(state: TaskEdgeState) {
        state.watchRunnable?.let { mainHandler.removeCallbacks(it) }
        state.watchRunnable = null
    }

    private fun reconcileTaskInfo(taskInfo: ActivityManager.RunningTaskInfo, reason: String) {
        val state = taskStates[taskInfo.taskId] ?: return
        state.taskInfo = taskInfo
        val hanging = taskInfo.booleanFieldOrFalse("isHangingMode")
        if (!taskInfo.isVisible || !hanging) {
            clearTask(
                taskInfo.taskId,
                restoreAlpha = true,
                reason = "$reason visible=${taskInfo.isVisible} hanging=$hanging",
            )
            return
        }
    }

    private fun clearTask(taskId: Int, restoreAlpha: Boolean, reason: String) {
        val state = taskStates.remove(taskId) ?: return
        clearState(state, restoreAlpha, reason)
    }

    private fun clearState(state: TaskEdgeState, restoreAlpha: Boolean, reason: String) {
        cancelDim(state)
        cancelEdgeWatch(state)
        state.iconOverlay?.remove()
        state.iconOverlay = null
        if (restoreAlpha) applyTaskAlpha(state, 1f)
        Log.i(TAG, "clear task state task=${state.taskId} reason=$reason")
    }

    private fun reposition(controller: Any, taskId: Int, start: Rect, end: Rect, moved: Boolean): Boolean {
        val positioner = controller.fieldOrNull("mHangingModePositioner") ?: return false
        return runCatching {
            XposedHelpers.callMethod(positioner, "reposition", taskId, Rect(start), Rect(end), moved)
        }.onFailure {
            Log.w(TAG, "reposition failed task=$taskId start=$start end=$end", it)
        }.isSuccess
    }

    private fun getHangingBounds(controller: Any, taskId: Int): Rect? {
        val positioner = controller.fieldOrNull("mHangingModePositioner") ?: return null
        return runCatching {
            XposedHelpers.callMethod(positioner, "getHangingBounds", taskId) as? Rect
        }.getOrNull()
    }

    private fun getStableBounds(controller: Any): Rect? {
        val positioner = controller.fieldOrNull("mHangingModePositioner") ?: return null
        return (positioner.fieldOrNull("mScreenStableBounds") as? Rect)?.takeUnless { it.isEmpty }
    }

    private fun sideFromBounds(controller: Any, bounds: Rect, ctrlType: Int): Int {
        val stableBounds = getStableBounds(controller)
        if (stableBounds != null) {
            return if (bounds.centerX() < stableBounds.centerX()) SIDE_LEFT else SIDE_RIGHT
        }
        return if ((ctrlType and 2) != 0) SIDE_LEFT else SIDE_RIGHT
    }

    private fun findTaskSurface(controller: Any, taskId: Int): SurfaceControl? {
        val decorations = controller.fieldOrNull("windowDecorationList") ?: return null
        val decoration = runCatching {
            XposedHelpers.callMethod(decorations, "get", taskId)
        }.getOrNull() ?: return null
        return decoration.fieldOrNull("mTaskSurface") as? SurfaceControl
    }

    private fun getRunningTaskInfo(controller: Any, taskId: Int): ActivityManager.RunningTaskInfo? {
        val organizer = controller.fieldOrNull("mShellTaskOrganizer") ?: return null
        return runCatching {
            XposedHelpers.callMethod(organizer, "getRunningTaskInfo", taskId) as? ActivityManager.RunningTaskInfo
        }.getOrNull()
    }

    private fun showEdgeIcon(state: TaskEdgeState, alpha: Float): Boolean {
        val controller = state.controller ?: return false
        val context = getControllerContext(controller) ?: return false
        val taskInfo = state.taskInfo ?: getRunningTaskInfo(controller, state.taskId)
        val icon = loadTaskIcon(controller, state.taskId, taskInfo)
        val overlay = state.iconOverlay ?: EdgeIconOverlay(context, state).also {
            state.iconOverlay = it
        }
        return overlay.show(state.iconBounds, icon, alpha)
    }

    private fun loadTaskIcon(
        controller: Any,
        taskId: Int,
        taskInfo: ActivityManager.RunningTaskInfo?,
    ): Bitmap? {
        val decorations = controller.fieldOrNull("windowDecorationList") ?: return null
        val decoration = runCatching {
            XposedHelpers.callMethod(decorations, "get", taskId)
        }.getOrNull() ?: return null
        val loader = decoration.fieldOrNull("mTaskResourceLoader") ?: return null
        return runCatching {
            XposedHelpers.callMethod(loader, "getVeilIcon", taskInfo) as? Bitmap
        }.getOrNull()
    }

    private fun applyTaskAlpha(state: TaskEdgeState, alpha: Float) {
        val surface = state.taskSurface ?: return
        if (!surface.isValid) return
        runCatching {
            SurfaceControl.Transaction().use { transaction ->
                transaction.setAlpha(surface, alpha.coerceIn(0f, 1f))
                transaction.apply()
            }
        }.onFailure { Log.w(TAG, "apply alpha failed task=${state.taskId}", it) }
    }

    private fun getControllerContext(controller: Any): Context? =
        controller.fieldOrNull("mDesktopTasksController")
            ?.fieldOrNull("context") as? Context

    private fun dp(controller: Any, value: Int): Float {
        val density = getControllerContext(controller)?.resources?.displayMetrics?.density ?: 1f
        return value * density
    }

    private fun Any.fieldOrNull(name: String): Any? =
        runCatching { XposedHelpers.getObjectField(this, name) }.getOrNull()

    private fun Any.booleanFieldOrFalse(name: String): Boolean =
        runCatching { XposedHelpers.getBooleanField(this, name) }.getOrDefault(false)

    private fun rectNear(a: Rect, b: Rect, tolerance: Int): Boolean =
        abs(a.left - b.left) <= tolerance &&
            abs(a.top - b.top) <= tolerance &&
            abs(a.right - b.right) <= tolerance &&
            abs(a.bottom - b.bottom) <= tolerance

    private fun Int.label(): String = if (this == SIDE_LEFT) "left" else "right"

    private enum class EdgeState {
        HANGING,
        EDGE_HALF,
        EDGE_DIM,
    }

    private data class TaskEdgeState(
        val taskId: Int,
        var controller: Any?,
        var edgeState: EdgeState,
        var side: Int,
        var hangingBounds: Rect,
        var edgeBounds: Rect,
        var iconBounds: Rect,
        var taskInfo: ActivityManager.RunningTaskInfo?,
        var taskSurface: SurfaceControl?,
        var iconOverlay: EdgeIconOverlay? = null,
        var dimRunnable: Runnable? = null,
        var watchRunnable: Runnable? = null,
        var lastInteractionTime: Long = System.currentTimeMillis(),
    )

    private class EdgeIconOverlay(
        private val baseContext: Context,
        private val state: TaskEdgeState,
    ) : View.OnTouchListener {
        private val windowContext = baseContext.createWindowContext(
            baseContext.display,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            null,
        )
        private val windowManager = windowContext.getSystemService(WindowManager::class.java)
        private val imageView = ImageView(windowContext)
        private val stableBounds: Rect = state.controller?.let { getStableBounds(it) } ?: Rect()
        private var layoutParams: WindowManager.LayoutParams? = null
        private var attached = false
        private var downRawX = 0f
        private var downRawY = 0f
        private var startX = 0
        private var startY = 0
        private var downTime = 0L
        private var moved = false

        fun show(bounds: Rect, icon: Bitmap?, alpha: Float): Boolean {
            if (icon != null) imageView.setImageBitmap(icon)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.alpha = alpha
            imageView.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = bounds.width() * 0.22f
                setColor(0xE6FFFFFF.toInt())
            }
            imageView.clipToOutline = true
            imageView.setOnTouchListener(this)

            val params = WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = bounds.left
                y = bounds.top
                title = "ZUXOS Vivo Hanging Icon"
            }
            layoutParams = params
            if (attached) {
                runCatching {
                    windowManager.updateViewLayout(imageView, params)
                }.onFailure {
                    Log.w(TAG, "update edge icon overlay failed", it)
                    return false
                }
            } else {
                runCatching {
                    windowManager.addView(imageView, params)
                }.onFailure {
                    Log.w(TAG, "add edge icon overlay failed", it)
                    return false
                }
                attached = true
            }
            return true
        }

        fun setAlpha(alpha: Float) {
            runOnViewThread {
                if (!attached) return@runOnViewThread
                imageView.animate().cancel()
                imageView.animate().alpha(alpha.coerceIn(0f, 1f)).setDuration(180L).start()
            }
        }

        fun remove() {
            runOnViewThread {
                if (!attached) return@runOnViewThread
                imageView.animate().cancel()
                imageView.setOnTouchListener(null)
                runCatching { windowManager.removeViewImmediate(imageView) }
                    .onFailure { Log.w(TAG, "remove edge icon overlay failed", it) }
                attached = false
            }
        }

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    cancelDim(state)
                    setAlpha(EDGE_HALF_ALPHA)
                    downRawX = event.rawX
                    downRawY = event.rawY
                    downTime = event.eventTime
                    moved = false
                    layoutParams?.let {
                        startX = it.x
                        startY = it.y
                    }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - downRawY
                    val dx = event.rawX - downRawX
                    if (abs(dx) + abs(dy) > 8f) moved = true
                    updatePosition(startX + dx.toInt(), startY + dy.toInt())
                    state.iconBounds.set(
                        layoutParams?.x ?: state.iconBounds.left,
                        layoutParams?.y ?: state.iconBounds.top,
                        (layoutParams?.x ?: state.iconBounds.left) + state.iconBounds.width(),
                        (layoutParams?.y ?: state.iconBounds.top) + state.iconBounds.height(),
                    )
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    val movedDistance = abs(dx) + abs(dy)
                    snapToEdge()
                    if (shouldRestoreNormalFromIconDrag(dx, dy)) {
                        mainHandler.post { restoreToNormal(state) }
                    } else if (event.eventTime - downTime <= 260L && movedDistance <= 24f) {
                        mainHandler.post { restoreToHanging(state) }
                    } else {
                        scheduleDim(state)
                    }
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    snapToEdge()
                    scheduleDim(state)
                    return true
                }
            }
            return true
        }

        private fun updatePosition(nextX: Int, nextY: Int) {
            val params = layoutParams ?: return
            val bounds = stableBounds.takeUnless { it.isEmpty }
            val minY = bounds?.top ?: 0
            val maxY = bounds?.let { it.bottom - params.height } ?: Int.MAX_VALUE
            params.y = nextY.coerceIn(minY, maxY.coerceAtLeast(minY))
            val visibleWidth = max(EDGE_MIN_VISIBLE_DP, min((params.width * 0.52f).toInt(), EDGE_MAX_VISIBLE_DP))
            params.x = if (state.side == SIDE_LEFT) {
                (bounds?.left ?: 0) - params.width + visibleWidth
            } else {
                (bounds?.right ?: startX + params.width) - visibleWidth
            }
            runOnViewThread {
                if (!attached) return@runOnViewThread
                runCatching { windowManager.updateViewLayout(imageView, params) }
                    .onFailure { Log.w(TAG, "drag edge icon update failed", it) }
            }
        }

        private fun snapToEdge() {
            val params = layoutParams ?: return
            updatePosition(params.x, params.y)
            state.iconBounds.set(params.x, params.y, params.x + params.width, params.y + params.height)
        }

        private fun shouldRestoreNormalFromIconDrag(dx: Float, dy: Float): Boolean {
            val inward = if (state.side == SIDE_LEFT) dx > 42f else dx < -42f
            return inward && dy > 48f
        }

        private fun runOnViewThread(action: () -> Unit) {
            val handler = imageView.handler
            if (handler == null || Looper.myLooper() == handler.looper) {
                action()
            } else {
                handler.post(action)
            }
        }
    }

    private const val EDGE_PUSH_THRESHOLD_DP = 32
    private const val EDGE_ICON_SIZE_DP = 64
    private const val EDGE_MIN_VISIBLE_DP = 28
    private const val EDGE_MAX_VISIBLE_DP = 48
    private const val EDGE_WATCH_INITIAL_DELAY_MS = 700L
    private const val EDGE_WATCH_INTERVAL_MS = 700L
}
