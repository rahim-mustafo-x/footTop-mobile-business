package uz.coder.foottopbusiness.presentation.main.home.user

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

interface UserCreateContract {
    data class State(
        val fullName: String = "",
        val phone: String = "",
        val role: String = "USER",
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false
    ) : MviState

    sealed interface Event : MviEvent {
        data class FullNameChanged(val name: String) : Event
        data class PhoneChanged(val phone: String) : Event
        data class RoleChanged(val role: String) : Event
        data object CreateClicked : Event
    }

    sealed interface Effect : MviEffect {
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
    }
}
