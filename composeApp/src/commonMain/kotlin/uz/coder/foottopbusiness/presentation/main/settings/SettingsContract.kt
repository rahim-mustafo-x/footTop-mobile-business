package uz.coder.foottopbusiness.presentation.main.settings

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.UserDto

sealed interface SettingsContract {
    data class State(
        val user: UserDto? = null,
        val isLoadingUser: Boolean = false,
        val showAboutDialog: Boolean = false,
        val showDeleteAccountDialog: Boolean = false,
        val deleteConfirmText: String = "",
        val isDeleting: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateToAuth : Effect
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object Logout : Event
        object ShowAboutApp : Event
        object DismissAboutDialog : Event
        object ShowDeleteAccount : Event
        object DismissDeleteAccount : Event
        data class UpdateDeleteConfirmText(val text: String) : Event
        object DeleteAccount : Event
    }
}
