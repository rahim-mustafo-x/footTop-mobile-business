package uz.coder.foottopbusiness.presentation.main.settings

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.UserDto

sealed interface SettingsContract {
    data class State(
        val user: UserDto? = null,
        val isLoadingUser: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateToAuth : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object Logout : Event
    }
}
