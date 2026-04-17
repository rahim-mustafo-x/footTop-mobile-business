package uz.coder.foottopbusiness.presentation.auth.login

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface LoginContract {
    data class State(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false
    ): MviState

    sealed interface Effect: MviEffect{
        data object NavigateToMain: Effect
        data object NavigateBack: Effect
        data class ShowToast(val message: String): Effect
    }

    sealed interface Event: MviEvent{
        data class UsernameChanged(val username: String): Event
        data class PasswordChanged(val password: String): Event
        data object LoginClicked: Event
        data object BackClicked: Event
    }
}
