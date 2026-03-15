package uz.coder.foottopbusiness.presentation.auth.otp

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface SendOtpContract {
    data class State(
        val phoneNumber: String = "",
        val isLoading: Boolean = false
    ): MviState
    sealed interface Effect: MviEffect{
        data class NavigateToLogin(val phoneNumber: String): Effect
        data class ShowToast(val message: String): Effect
        data class Error(val message: String?): Effect
        data class Logged(val logged: Boolean): Effect
    }
    sealed interface Event: MviEvent{
        data object NavigateToLogin: Event
        data class TypePhoneNumber(val phoneNumber: String): Event
        data object Load: Event
    }
}