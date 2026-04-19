package uz.coder.foottopbusiness.presentation.main.settings

import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.domain.usecase.auth.ChangePasswordUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val getUserUseCase: GetUserUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
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
                sendEffect(SettingsContract.Effect.NavigateToAbout)
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
            SettingsContract.Event.ShowChangePassword -> {
                updateState { copy(showChangePasswordDialog = true, oldPassword = "", newPassword = "", confirmPassword = "") }
            }
            SettingsContract.Event.DismissChangePassword -> {
                updateState { copy(showChangePasswordDialog = false) }
            }
            is SettingsContract.Event.UpdateOldPassword -> {
                updateState { copy(oldPassword = event.text) }
            }
            is SettingsContract.Event.UpdateNewPassword -> {
                updateState { copy(newPassword = event.text) }
            }
            is SettingsContract.Event.UpdateConfirmPassword -> {
                updateState { copy(confirmPassword = event.text) }
            }
            SettingsContract.Event.ChangePassword -> {
                val state = state.value
                if (state.oldPassword.isEmpty() || state.newPassword.isEmpty() || state.confirmPassword.isEmpty()) {
                    sendEffect(SettingsContract.Effect.ShowToast("Barcha maydonlarni to'ldiring"))
                    return
                }
                if (state.newPassword != state.confirmPassword) {
                    sendEffect(SettingsContract.Effect.ShowToast("Parollar mos kelmadi"))
                    return
                }
                updateState { copy(isChangingPassword = true) }
                executeAsync(
                    block = {
                        var success = false
                        changePasswordUseCase(state.oldPassword.trim(), state.newPassword.trim()).collect {
                            success = true
                        }
                        success
                    },
                    onSuccess = {
                        updateState { copy(isChangingPassword = false, showChangePasswordDialog = false) }
                        sendEffect(SettingsContract.Effect.ShowToast("Parol muvaffaqiyatli o'zgartirildi"))
                    },
                    onError = {
                        updateState { copy(isChangingPassword = false) }
                        sendEffect(SettingsContract.Effect.ShowToast("Xatolik yuz berdi. Balki eski parol noto'g'ridir."))
                    }
                )
            }
        }
    }
}
