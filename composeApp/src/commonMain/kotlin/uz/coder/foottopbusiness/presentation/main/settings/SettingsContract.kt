package uz.coder.foottopbusiness.presentation.main.settings

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.UserDto

sealed interface SettingsContract {
    data class State(
        val user: UserDto? = null,
        val isLoadingUser: Boolean = false,
        val showDeleteAccountDialog: Boolean = false,
        val deleteConfirmText: String = "",
        val isDeleting: Boolean = false,
        val showChangePasswordDialog: Boolean = false,
        val oldPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val isChangingPassword: Boolean = false
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateToAuth : Effect
        object NavigateToAbout : Effect
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object Logout : Event
        object ShowAboutApp : Event
        object ShowDeleteAccount : Event
        object DismissDeleteAccount : Event
        data class UpdateDeleteConfirmText(val text: String) : Event
        object DeleteAccount : Event
        object ShowChangePassword : Event
        object DismissChangePassword : Event
        data class UpdateOldPassword(val text: String) : Event
        data class UpdateNewPassword(val text: String) : Event
        data class UpdateConfirmPassword(val text: String) : Event
        object ChangePassword : Event
    }
}
