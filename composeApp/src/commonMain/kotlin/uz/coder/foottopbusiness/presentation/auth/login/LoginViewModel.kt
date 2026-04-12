package uz.coder.foottopbusiness.presentation.auth.login

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.repository.LoginResult
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase
import uz.coder.foottopbusiness.domain.usecase.notification.RegisterDeviceTokenUseCase
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.data.local.PreferencesManager
import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.core.platform.getPlatform
import uz.coder.foottopbusiness.core.notification.PushTokenProvider

@Suppress("EQUALITY_NOT_APPLICABLE_WARNING")
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val preferencesManager: PreferencesManager,
    private val pushTokenProvider: PushTokenProvider
): BaseViewModel<LoginContract.State, LoginContract.Effect, LoginContract.Event>(initialState = LoginContract.State()) {
    override fun handleEvent(event: LoginContract.Event) {
        when(event){
            is LoginContract.Event.PhoneNumber -> updateState { copy(phoneNumber = event.phoneNumber) }
            is LoginContract.Event.OtpCode -> {
                val text = event.otpCode.take(4).filter { it.isDigit() }
                updateState { copy(otpCode = text) }
                if (text.length >= 4) login()
            }
            LoginContract.Event.TimerTick -> updateState {
                if (secondsLeft > 0) {
                    val newSeconds = secondsLeft - 1
                    copy(secondsLeft = newSeconds, canResend = newSeconds == 0)
                } else this
            }
            LoginContract.Event.ResendCode ->
                viewModelScope.launch {
                    val rawNumber = state.value.phoneNumber.removePrefix("+998")
                    sendOtpUseCase(rawNumber).collect {
                        if (it) {
                            updateState {
                                copy(secondsLeft = 60, canResend = false, otpCode = "")
                            }
                        }
                    }
                }
        }
    }

    private fun login() {
        executeAsync(
            onError = { sendEffect(LoginContract.Effect.ShowToast(it.message ?: "Xatolik yuz berdi")) },
            onSuccess = { result ->
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
                                            deviceType = if (getPlatform().name.contains(
                                                    "Android",
                                                    ignoreCase = true
                                                )
                                            ) "ANDROID" else "IOS"
                                        )
                                    ).collect { }
                                }
                            }
                        }
                        sendEffect(LoginContract.Effect.NavigateToMain)
                    }
                    LoginResult.RegisterRequired -> sendEffect(
                        LoginContract.Effect.ShowToast("Ro'yxatdan o'tish talab qilinadi. Iltimos, qayta ro'yxatdan o'ting.")
                    )
                    LoginResult.InvalidOtp -> sendEffect(
                        LoginContract.Effect.ShowToast("Kod noto'g'ri kiritildi. Qayta urinib ko'ring.")
                    )
                }
            }
        ) {
            val s = state.first()
            loginUseCase(s.phoneNumber, s.otpCode).first()
        }
    }
}