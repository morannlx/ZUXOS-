package me.inkdye.zuxos.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.inkdye.zuxos.root.RestartScope
import me.inkdye.zuxos.root.RootCommandResult
import me.inkdye.zuxos.root.RootManager
import me.inkdye.zuxos.root.RootState
import me.inkdye.zuxos.root.RootStatus

class RootUiState {
    val state: MutableState<RootState> = mutableStateOf(RootState(RootStatus.UNKNOWN))
    val busy: MutableState<Boolean> = mutableStateOf(false)

    fun refresh(scope: CoroutineScope) {
        scope.launch {
            busy.value = true
            state.value = RootManager.queryState()
            busy.value = false
        }
    }

    fun request(scope: CoroutineScope) {
        scope.launch {
            busy.value = true
            state.value = RootManager.requestAccess()
            busy.value = false
        }
    }

    fun restart(scope: CoroutineScope, restartScope: RestartScope, onResult: (RootCommandResult) -> Unit) {
        scope.launch {
            busy.value = true
            val current = RootManager.requestAccess()
            state.value = current
            val result = if (current.isGranted) {
                RootManager.restart(restartScope)
            } else {
                RootCommandResult(
                    success = false,
                    exitCode = -1,
                    output = "",
                    error = current.message ?: current.status.name,
                )
            }
            busy.value = false
            onResult(result)
        }
    }

    fun execute(scope: CoroutineScope, command: String, onResult: (RootCommandResult) -> Unit) {
        scope.launch {
            busy.value = true
            val current = RootManager.requestAccess()
            state.value = current
            val result = if (current.isGranted) {
                RootManager.execute(command)
            } else {
                RootCommandResult(
                    success = false,
                    exitCode = -1,
                    output = "",
                    error = current.message ?: current.status.name,
                )
            }
            busy.value = false
            onResult(result)
        }
    }
}

@Composable
fun rememberRootUiState(): RootUiState {
    val rootState = remember { RootUiState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(rootState) {
        rootState.refresh(this)
    }

    DisposableEffect(lifecycleOwner, rootState) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                rootState.refresh(coroutineScope)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return rootState
}
