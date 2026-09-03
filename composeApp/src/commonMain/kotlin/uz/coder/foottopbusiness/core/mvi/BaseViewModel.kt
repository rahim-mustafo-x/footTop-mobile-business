package uz.coder.foottopbusiness.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Ekran ViewModel'lari uchun asos.
 *
 * ScreenModel'ni ham implement qiladi, shunda ekranni `getScreenModel()` orqali
 * olish mumkin bo'ladi. Bu muhim: `koinInject()` ViewModel'ni oddiy `remember`da
 * saqlaydi, Voyager esa yangi ekran push qilinganda avvalgi ekran
 * kompozitsiyasini tashlab yuboradi - qaytib kelganda Koin `factory` yangi
 * ViewModel yasab, forma to'ldirilgan ma'lumotlar yo'qolardi. ScreenModelStore
 * esa nusxani ekran kalitiga bog'lab saqlaydi va faqat ekran pop bo'lganda
 * tozalaydi.
 */
abstract class BaseViewModel <S: MviState, E: MviEffect, A: MviEvent>(initialState: S): ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()
    private val _effect = Channel<E>(Channel.BUFFERED)

    /**
     * Bir martalik effektlar oqimi.
     *
     * DIQQAT: Channel asosida qurilgan, ya'ni **bitta iste'molchi** uchun.
     * Bitta ViewModel nusxasini ikki joyda collect qilsa, event'lar ular
     * o'rtasida bo'linib ketadi va bir qismi yo'qoladi.
     * Har bir ViewModel nusxasiga bitta collector bo'lsin.
     */
    val effect = _effect.receiveAsFlow()
    protected fun updateState(reducer: S.() -> S){
        _state.value = _state.value.reducer()
    }
    protected fun sendEffect(effect: E){
        _effect.trySend(effect)
    }
    abstract fun handleEvent(event: A)

    /**
     * Ekran pop bo'lganda ScreenModelStore chaqiradi. ViewModel'ning `onCleared()`i
     * bu yo'l bilan ishga tushmaydi, shuning uchun scope'ni o'zimiz yopamiz.
     */
    override fun onDispose() {
        viewModelScope.cancel()
    }

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
