package me.inkdye.zuxos.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class BooleanSettingState(
    private val repository: SettingsRepository,
    private val spec: BooleanSettingSpec,
) {
    val checked: MutableState<Boolean> = mutableStateOf(repository.getBoolean(spec))

    fun update(value: Boolean) {
        checked.value = value
        repository.setBoolean(spec, value)
    }

    fun reload() {
        checked.value = repository.getBoolean(spec)
    }
}

@Composable
fun rememberSettingsRepository(): SettingsRepository {
    val context = LocalContext.current
    return remember(context) { SettingsRepository(context) }
}

@Composable
fun rememberBooleanSettingState(
    repository: SettingsRepository,
    spec: BooleanSettingSpec,
): BooleanSettingState {
    val state = remember(repository, spec) {
        BooleanSettingState(repository, spec)
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, state) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state.reload()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return state
}
