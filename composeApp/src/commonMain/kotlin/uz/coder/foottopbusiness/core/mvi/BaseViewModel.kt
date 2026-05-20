package uz.coder.foottopbusiness.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel <S: MviState, E: MviEffect, A: MviEvent>(initialState: S): ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    protected fun updateState(reducer: S.() -> S){
        _state.value = _state.value.reducer()
    }
    protected fun sendEffect(effect: E){
        _effect.trySend(effect)
    }
    abstract fun handleEvent(event: A)
    protected fun <T> executeAsync(
        onLoading: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
        onSuccess: suspend (T) -> Unit = {},
        block: suspend () -> T
    ): Job {
        return viewModelScope.launch {
            try {
                onLoading()
                val result = block()
                onSuccess(result)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
