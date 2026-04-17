package uz.coder.foottopbusiness.presentation.auth.login

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.repository.LoginResult
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.notification.RegisterDeviceTokenUseCase
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.data.local.PreferencesManager
import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.core.platform.getPlatform
import uz.coder.foottopbusiness.core.notification.PushTokenProvider

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val preferencesManager: PreferencesManager,
    private val pushTokenProvider: PushTokenProvider
): BaseViewModel<LoginContract.State, LoginContract.Effect, LoginContract.Event>(initialState = LoginContract.State()) {
    
    override fun handleEvent(event: LoginContract.Event) {
        when(event){
            is LoginContract.Event.UsernameChanged -> updateState { copy(username = event.username) }
            is LoginContract.Event.PasswordChanged -> updateState { copy(password = event.password) }
            LoginContract.Event.LoginClicked -> login()
            LoginContract.Event.BackClicked -> sendEffect(LoginContract.Effect.NavigateBack)
        }
    }

    private fun login() {
        if (state.value.username.isBlank() || state.value.password.isBlank()) {
            sendEffect(LoginContract.Effect.ShowToast("Iltimos, barcha maydonlarni to'ldiring"))
            return
        }

        updateState { copy(isLoading = true) }
        
        executeAsync(
            onError = { 
                updateState { copy(isLoading = false) }
                sendEffect(LoginContract.Effect.ShowToast(it.message ?: "Xatolik yuz berdi")) 
            },
            onSuccess = { result ->
                updateState { copy(isLoading = false) }
                when (result) {
                    LoginResult.Success -> {
                        viewModelScope.launch {
                            val userId = preferencesManager.userId.firstOrNull() ?: 0
                            if (userId.toLong() != 0L) {
                                val token = pushTokenProvider.getToken()
                                if (token != null) {
                                    registerDeviceTokenUseCase(
                                        DeviceTokenRequest(
                                            userId = userId.toLong(),
                                            token = token,
                                            deviceType = if (getPlatform().name.contains("Android", ignoreCase = true)) "ANDROID" else "IOS"
                                        )
                                    ).collect { }
                                }
                            }
                        }
                        sendEffect(LoginContract.Effect.NavigateToMain)
                    }
                    else -> sendEffect(LoginContract.Effect.ShowToast("Username yoki parol noto'g'ri"))
                }
            }
        ) {
            val s = state.first()
            loginUseCase(s.username.trim(), s.password.trim()).first()
        }
    }
}
