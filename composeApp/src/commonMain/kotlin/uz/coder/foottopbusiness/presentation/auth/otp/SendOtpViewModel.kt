package uz.coder.foottopbusiness.presentation.auth.otp

import kotlinx.coroutines.flow.first
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase

class SendOtpViewModel(
    private val sendOtpUseCase: SendOtpUseCase
): BaseViewModel<SendOtpContract.State, SendOtpContract.Effect, SendOtpContract.Event>(initialState = SendOtpContract.State()) {
    override fun handleEvent(event: SendOtpContract.Event) {
        when(event){
            SendOtpContract.Event.NavigateToLogin -> sendOtp()
            is SendOtpContract.Event.TypePhoneNumber -> updateState {
                copy(phoneNumber = event.phoneNumber.filter { it.isDigit() }.take(9))
            }
        }
    }

    private fun sendOtp() {
        executeAsync(onLoading = {
            sendEffect(SendOtpContract.Effect.Loading)
        },
            onError = {
                sendEffect(SendOtpContract.Effect.Error(it.message))
            },
            onSuccess = {result->
                if (result){
                    sendEffect(SendOtpContract.Effect.NavigateToLogin)
                }else{
                    sendEffect(SendOtpContract.Effect.ShowToast("Something went wrong"))
                }
            }){
            sendOtpUseCase(state.first().phoneNumber).first()
        }
    }
}