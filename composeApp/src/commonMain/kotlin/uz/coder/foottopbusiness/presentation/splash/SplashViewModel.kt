package uz.coder.foottopbusiness.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase

class SplashViewModel(
    private val isLoginInUseCase: IsLoginInUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val preferencesManager: PreferencesManager,
    private val userSession: UserSession
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
            
            if (isLoggedIn) {
                try {
                    val userId = preferencesManager.userId.first()
                    if (userId != 0) {
                        val user = getUserUseCase(userId.toLong()).first()
                        userSession.setUser(user)
                    }
                } catch (e: Exception) {
                    // If we can't load user info, we might want to logout or try again
                    // For now, if logged in, we try to proceed, but if user data is critical, 
                    // we could stay on Splash or go to Login.
                }
                delayJob.join()
                _navigationEvent.emit(SplashNavigationEvent.NavigateToMain)
            } else {
                userSession.clear()
                delayJob.join()
                _navigationEvent.emit(SplashNavigationEvent.NavigateToLogin)
            }
        }
    }
}

sealed interface SplashNavigationEvent {
    data object NavigateToMain : SplashNavigationEvent
    data object NavigateToLogin : SplashNavigationEvent
}
