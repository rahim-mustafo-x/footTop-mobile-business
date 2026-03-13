package uz.coder.foottopbusiness.presentation.auth.otp

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface SendOtpContract {
    data class State(
        val phoneNumber: String = ""
    ): MviState
    sealed interface Effect: MviEffect{
        data object NavigateToLogin: Effect
        data class ShowToast(val message: String): Effect
    }
    sealed interface Event: MviEvent{
        data object NavigateToLogin: Event
        data class TypePhoneNumber(val phoneNumber: String): Event
    }
}