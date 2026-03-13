package uz.coder.foottopbusiness.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel <S: MviState, E: MviEffect, A: MviEvent>(initialState: S): ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()
    val currentState = state.value
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
        onLoading:()->Unit={},
        onError:(Throwable)->Unit={},
        onSuccess:(T)->Unit={},
        block: suspend ()->T) {
        viewModelScope.launch {
            try {
                onLoading()
                val result = async { block() }
                onSuccess(result.await())
            }catch (e: Exception){
                onError(e)
            }
        }
    }
}