package uz.coder.foottopbusiness.presentation.auth.otp

import uz.coder.foottopbusiness.core.mvi.BaseViewModel

class SendOtpViewModel: BaseViewModel<SendOtpContract.State, SendOtpContract.Effect, SendOtpContract.Event>(initialState = SendOtpContract.State()) {
    override fun handleEvent(event: SendOtpContract.Event) {
        when(event){
            SendOtpContract.Event.NavigateToLogin -> sendEffect(SendOtpContract.Effect.NavigateToLogin)
            is SendOtpContract.Event.TypePhoneNumber -> updateState { copy(phoneNumber = event.phoneNumber) }
        }
    }
}