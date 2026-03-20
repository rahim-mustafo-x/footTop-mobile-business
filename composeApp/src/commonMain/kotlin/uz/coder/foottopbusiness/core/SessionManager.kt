package uz.coder.foottopbusiness.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SessionState {
    EXPIRED,
    UNEXPIRED
}

class SessionManager {
    // StateFlow — faqat oxirgi holat saqlanadi, eski EXPIRED event qayta trigger qilmaydi
    private val _sessionState = MutableStateFlow(SessionState.UNEXPIRED)
    val sessionState = _sessionState.asStateFlow()

    fun onUnauthorized() {
        _sessionState.value = SessionState.EXPIRED
    }

    fun onAuthorized() {
        _sessionState.value = SessionState.UNEXPIRED
    }
}
