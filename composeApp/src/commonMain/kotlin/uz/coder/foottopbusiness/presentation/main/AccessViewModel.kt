package uz.coder.foottopbusiness.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.usecase.auth.LogoutUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase

/**
 * Rol aniqlanmagan holatdan chiqish yo'llari.
 *
 * Splash foydalanuvchini yuklashda xatoni jimgina yutib yuboradi va baribir
 * asosiy ekranga o'tkazadi - u holda rol UNKNOWN bo'lib qoladi. Ilgari bunda
 * ilova cheksiz aylanuvchi indikatorda qotib qolardi, bu ViewModel esa ikkita
 * chiqish yo'lini beradi: qayta yuklash yoki hisobdan chiqish.
 */
class AccessViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val preferencesManager: PreferencesManager,
    private val userSession: UserSession,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _isRetrying = MutableStateFlow(false)
    val isRetrying = _isRetrying.asStateFlow()

    fun retry() {
        if (_isRetrying.value) return
        viewModelScope.launch {
            _isRetrying.value = true
            try {
                val userId = preferencesManager.userId.first()
                if (userId != 0) {
                    // setUser rolni aniqlab, UserSession'ga yozadi - MainScreen
                    // shu oqimni kuzatgani uchun ekran o'zi yangilanadi
                    userSession.setUser(getUserUseCase(userId.toLong()).first())
                }
            } catch (_: Exception) {
                // Xato bo'lsa ekran o'z holicha qoladi, foydalanuvchi yana
                // urinib ko'rishi yoki chiqishi mumkin
            }
            _isRetrying.value = false
        }
    }

    /**
     * Chiqishdan keyin alohida navigatsiya kerak emas: token tozalanadi,
     * AppNavigation esa token yo'qolganini ko'rib login ekraniga o'tkazadi.
     */
    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}
