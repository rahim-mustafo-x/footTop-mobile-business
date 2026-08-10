package uz.coder.foottopbusiness.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LogoutUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase

class SplashViewModel(
    private val isLoginInUseCase: IsLoginInUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val preferencesManager: PreferencesManager,
    private val userSession: UserSession,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    fun retry() {
        if (_uiState.value == SplashUiState.Loading) return
        checkLoginStatus()
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }

    /**
     * Sessiyani tekshirib, foydalanuvchi profilini yuklaydi.
     *
     * Profil yuklanmasa asosiy ekranga o'tilmaydi. Ilgari bu yerdagi xato
     * bo'sh `catch` bilan yutilar va ilova baribir Main'ga o'tardi - u yerda
     * esa rol UNKNOWN bo'lgani uchun ekran cheksiz aylanardi. Ilovaning har
     * bir bo'limi rolga tayangani sababli, rolsiz ichkariga kiritishning
     * ma'nosi yo'q: xatoni shu yerda ko'rsatib, qayta urinish taklif qilamiz.
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading

            // Splash kamida 2 soniya ko'rinib tursin
            val delayJob = launch { delay(2000) }

            val isLoggedIn = isLoginInUseCase().first()
            if (!isLoggedIn) {
                userSession.clear()
                delayJob.join()
                _navigationEvent.emit(SplashNavigationEvent.NavigateToLogin)
                return@launch
            }

            val userId = preferencesManager.userId.first()
            if (userId == 0) {
                // Token bor, lekin foydalanuvchi id yo'q - sessiya nuqsonli,
                // uni faqat qaytadan kirish tuzatadi
                delayJob.join()
                _uiState.value = SplashUiState.ProfileLoadFailed
                return@launch
            }

            val user = try {
                getUserUseCase(userId.toLong()).first()
            } catch (_: Exception) {
                delayJob.join()
                _uiState.value = SplashUiState.ProfileLoadFailed
                return@launch
            }

            // setUser rolni aniqlab, prefs'ga ham yozadi
            userSession.setUser(user)
            delayJob.join()
            _navigationEvent.emit(SplashNavigationEvent.NavigateToMain)
        }
    }
}

sealed interface SplashUiState {
    data object Loading : SplashUiState

    /** Profil yuklanmadi - tarmoq yoki sessiya muammosi. */
    data object ProfileLoadFailed : SplashUiState
}

sealed interface SplashNavigationEvent {
    data object NavigateToMain : SplashNavigationEvent
    data object NavigateToLogin : SplashNavigationEvent
}
