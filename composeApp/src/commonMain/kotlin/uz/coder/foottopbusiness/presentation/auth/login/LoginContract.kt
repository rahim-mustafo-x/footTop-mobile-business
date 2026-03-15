package uz.coder.foottopbusiness.presentation.auth.login

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface LoginContract {
    data class State(
        val phoneNumber: String = "",
        val otpCode: String = "",
        val secondsLeft: Int = 60,
        val canResend: Boolean = false
    ): MviState
    sealed interface Effect: MviEffect{
        data object NavigateToMain: Effect
        data class ShowToast(val message: String): Effect
    }
    sealed interface Event: MviEvent{
        data class PhoneNumber(val phoneNumber: String): Event
        data class OtpCode(val otpCode: String): Event
        data object TimerTick: Event
        data object ResendCode: Event
    }
}