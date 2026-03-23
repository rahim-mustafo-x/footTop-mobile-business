package uz.coder.foottopbusiness.presentation.auth.otp

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LogoutUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase

class SendOtpViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
    private val isLoginInUseCase: IsLoginInUseCase,
    private val logoutUseCase: LogoutUseCase
): BaseViewModel<SendOtpContract.State, SendOtpContract.Effect, SendOtpContract.Event>(initialState = SendOtpContract.State()) {
    override fun handleEvent(event: SendOtpContract.Event) {
        when(event){
            SendOtpContract.Event.NavigateToLogin -> {
                viewModelScope.launch {
                    logoutUseCase()
                    sendOtp()
                }
            }
            is SendOtpContract.Event.TypePhoneNumber -> updateState {
                copy(phoneNumber = event.phoneNumber.filter { it.isDigit() }.take(9))
            }
            SendOtpContract.Event.Load -> {
                viewModelScope.launch {
                    isLoginInUseCase().collect {
                        sendEffect(SendOtpContract.Effect.Logged(it))
                    }
                }
            }

        }
    }

    private fun sendOtp() {
        executeAsync(onLoading = {
            updateState { copy(isLoading = true) }
        },
            onError = {
                updateState { copy(isLoading = false) }
                sendEffect(SendOtpContract.Effect.Error(it.message))
            },
            onSuccess = { result ->
                updateState { copy(isLoading = false) }
                if (result) {
                    sendEffect(SendOtpContract.Effect.NavigateToLogin("+998${state.value.phoneNumber}"))
                } else {
                    sendEffect(SendOtpContract.Effect.ShowToast("Something went wrong"))
                }
            }) {
            sendOtpUseCase(state.value.phoneNumber).first()
        }
    }
}