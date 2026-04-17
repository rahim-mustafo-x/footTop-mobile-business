package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface LoginResult {
    data object Success : LoginResult
    data object RegisterRequired : LoginResult
    data object InvalidOtp : LoginResult
}

interface AuthRepository {
    fun staffLogin(username: String, password: String): Flow<LoginResult>
    fun isLoginIn(): Flow<Boolean>
    suspend fun logout()
}