// EditProfileContract.kt
package uz.coder.foottopbusiness.presentation.main.settings.editprofile

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.UserDto

object EditProfileContract {
    data class State(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val username: String = "",
        val fullName: String = "",
        val phone: String = "",
        val location: String = "",
        val error: String? = null,
        val user: UserDto? = null,
    ): MviState

    sealed interface Event: MviEvent {
        data class UsernameChanged(val value: String) : Event
        data class FullNameChanged(val value: String) : Event
        data class LocationChanged(val value: String) : Event
        data object Save : Event
    }

    sealed interface Effect: MviEffect {
        data object SavedSuccessfully : Effect
        data class ShowError(val message: String) : Effect
    }
}