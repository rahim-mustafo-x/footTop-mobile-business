package uz.coder.foottopbusiness.presentation.main.settings

import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val getUserUseCase: GetUserUseCase,
) : BaseViewModel<SettingsContract.State, SettingsContract.Effect, SettingsContract.Event>(
    initialState = SettingsContract.State()
) {
    init { handleEvent(SettingsContract.Event.Load) }

    override fun handleEvent(event: SettingsContract.Event) {
        when (event) {
            SettingsContract.Event.Load -> {
                updateState { copy(isLoadingUser = true) }
                executeAsync(
                    block = {
                        val userId = preferencesManager.userId.firstOrNull() ?: 0
                        var result: UserDto? = null
                        getUserUseCase(userId.toLong()).collect { result = it }
                        result
                    },
                    onSuccess = { updateState { copy(user = it, isLoadingUser = false) } },
                    onError = { updateState { copy(isLoadingUser = false) } }
                )
            }
            SettingsContract.Event.Logout -> executeAsync(
                block = {
                    preferencesManager.setAuthorised(false)
                    preferencesManager.setToken("")
                },
                onSuccess = { sendEffect(SettingsContract.Effect.NavigateToAuth) }
            )
            SettingsContract.Event.ShowAboutApp -> {
                updateState { copy(showAboutDialog = true) }
            }
            SettingsContract.Event.DismissAboutDialog -> {
                updateState { copy(showAboutDialog = false) }
            }
            SettingsContract.Event.ShowDeleteAccount -> {
                updateState { copy(showDeleteAccountDialog = true, deleteConfirmText = "") }
            }
            SettingsContract.Event.DismissDeleteAccount -> {
                updateState { copy(showDeleteAccountDialog = false, deleteConfirmText = "") }
            }
            is SettingsContract.Event.UpdateDeleteConfirmText -> {
                updateState { copy(deleteConfirmText = event.text) }
            }
            SettingsContract.Event.DeleteAccount -> {
                val state = state.value
                val username = state.user?.username ?: ""
                if (state.deleteConfirmText == username) {
                    updateState { copy(isDeleting = true) }
                    executeAsync(
                        block = {
                            // TODO: Add DeleteAccountUseCase
                            preferencesManager.setAuthorised(false)
                            preferencesManager.setToken("")
                        },
                        onSuccess = {
                            updateState { copy(isDeleting = false, showDeleteAccountDialog = false) }
                            sendEffect(SettingsContract.Effect.NavigateToAuth)
                        },
                        onError = {
                            updateState { copy(isDeleting = false) }
                            sendEffect(SettingsContract.Effect.ShowToast("Xatolik yuz berdi"))
                        }
                    )
                } else {
                    sendEffect(SettingsContract.Effect.ShowToast("Username noto'g'ri kiritildi"))
                }
            }
        }
    }
}
