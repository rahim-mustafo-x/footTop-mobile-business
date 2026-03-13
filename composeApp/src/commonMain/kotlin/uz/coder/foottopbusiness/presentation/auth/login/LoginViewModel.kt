package uz.coder.foottopbusiness.presentation.auth.login

import uz.coder.foottopbusiness.core.mvi.BaseViewModel

class LoginViewModel: BaseViewModel<LoginContract.State, LoginContract.Effect, LoginContract.Event>(initialState = LoginContract.State()) {
    override fun handleEvent(event: LoginContract.Event) {
        when(event){
            LoginContract.Event.NavigateToMain -> TODO()
        }
    }
}