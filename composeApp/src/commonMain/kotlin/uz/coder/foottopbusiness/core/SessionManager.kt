package uz.coder.foottopbusiness.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SessionState {
    data object Expired : SessionState()
    data object Unexpired : SessionState()
}

class SessionManager {
    // StateFlow — faqat oxirgi holat saqlanadi, eski EXPIRED event qayta trigger qilmaydi
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unexpired)
    val sessionState = _sessionState.asStateFlow()

    // Token validligini kuzatish uchun - bir marta true bo'lsa, qayta false ga o'zgartirmaydi
    private val _isTokenValid = MutableStateFlow(true)
    val isTokenValid = _isTokenValid.asStateFlow()

    // Token kuzatilayotganmi - bir marta o'zgarish bo'lganda isTokenValid ni yangilaydi
    private var isObservingToken = false

    fun onUnauthorized() {
        _sessionState.value = SessionState.Expired
    }

    fun onAuthorized() {
        _sessionState.value = SessionState.Unexpired
        _isTokenValid.value = true
    }

    fun setTokenValid(valid: Boolean) {
        if (_isTokenValid.value && !valid) {
            // Tokenni bir marta invalid qilingandan so'nng qayta valid qilmaslik uchun
            _isTokenValid.value = valid
        }
    }

    fun startObservingToken() {
        isObservingToken = true
    }

    fun isObservingToken(): Boolean = isObservingToken
}
