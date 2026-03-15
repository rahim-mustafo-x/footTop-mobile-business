package uz.coder.foottopbusiness.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase

class SplashViewModel(
    private val isLoginInUseCase: IsLoginInUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            // Add a small delay for splash effect if needed
            val isLoggedIn = isLoginInUseCase().first()
            if (isLoggedIn) {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToMain)
            } else {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToLogin)
            }
        }
    }
}

sealed interface SplashNavigationEvent {
    data object NavigateToMain : SplashNavigationEvent
    data object NavigateToLogin : SplashNavigationEvent
}
