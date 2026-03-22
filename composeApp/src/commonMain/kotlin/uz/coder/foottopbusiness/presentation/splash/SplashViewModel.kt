package uz.coder.foottopbusiness.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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
            // Guarantee at least 2 seconds of splash screen
            val delayJob = launch { delay(2000) }
            
            // Get the first emitted value of login status
            val isLoggedIn = isLoginInUseCase().first()
            
            // Wait for the delay to finish if it hasn't already
            delayJob.join()

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
