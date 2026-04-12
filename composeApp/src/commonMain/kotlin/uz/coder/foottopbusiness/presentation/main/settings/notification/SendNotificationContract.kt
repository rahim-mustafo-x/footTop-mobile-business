package uz.coder.foottopbusiness.presentation.main.settings.notification

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface SendNotificationContract {
    data class State(
        val title: String = "",
        val body: String = "",
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false
    ) : MviState

    sealed interface Event : MviEvent {
        data class UpdateTitle(val title: String) : Event
        data class UpdateBody(val body: String) : Event
        data object SendToAll : Event
        data object ResetSuccess : Event
    }

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data object NavigateBack : Effect
    }
}
