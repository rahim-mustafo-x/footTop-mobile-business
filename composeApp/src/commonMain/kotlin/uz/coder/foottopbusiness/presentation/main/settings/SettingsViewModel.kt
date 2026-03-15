package uz.coder.foottopbusiness.presentation.main.settings

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.local.PreferencesManager

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : BaseViewModel<SettingsContract.State, SettingsContract.Effect, SettingsContract.Event>(
    initialState = SettingsContract.State()
) {
    override fun handleEvent(event: SettingsContract.Event) {
        when (event) {
            SettingsContract.Event.Logout -> executeAsync(
                block = {
                    preferencesManager.setAuthorised(false)
                    preferencesManager.setToken("")
                },
                onSuccess = {
                    sendEffect(SettingsContract.Effect.NavigateToAuth)
                }
            )
        }
    }
}
