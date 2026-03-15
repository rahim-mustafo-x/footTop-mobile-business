package uz.coder.foottopbusiness.presentation.auth.login

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sendOtpUseCase: SendOtpUseCase
): BaseViewModel<LoginContract.State, LoginContract.Effect, LoginContract.Event>(initialState = LoginContract.State()) {
    override fun handleEvent(event: LoginContract.Event) {
        when(event){
            is LoginContract.Event.PhoneNumber -> updateState { copy(phoneNumber = event.phoneNumber) }
            is LoginContract.Event.OtpCode -> {
                val text = event.otpCode.take(6).filter { it.isDigit() }
                updateState { copy(otpCode = text) }
                if (text.length >= 6) login()
            }
            LoginContract.Event.TimerTick -> updateState {
                if (secondsLeft > 0) {
                    val newSeconds = secondsLeft - 1
                    copy(secondsLeft = newSeconds, canResend = newSeconds == 0)
                } else this
            }
            LoginContract.Event.ResendCode ->
                viewModelScope.launch {
                    val rawNumber = state.value.phoneNumber.removePrefix("+998")
                    sendOtpUseCase(rawNumber).collect {
                        if (it) {
                            updateState {
                                copy(secondsLeft = 60, canResend = false, otpCode = "")
                            }
                        }
                    }
                }
        }
    }

    private fun login() {
        executeAsync(
            onError = { sendEffect(LoginContract.Effect.ShowToast(it.message ?: "")) },
            onSuccess = { success ->
                if (success) sendEffect(LoginContract.Effect.NavigateToMain)
                else sendEffect(LoginContract.Effect.ShowToast("Noto'g'ri kod"))
            }
        ) {
            val s = state.first()
            loginUseCase(s.phoneNumber, s.otpCode).first()
        }
    }
}