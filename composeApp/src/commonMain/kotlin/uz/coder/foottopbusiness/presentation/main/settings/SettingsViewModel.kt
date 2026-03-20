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
        }
    }
}
