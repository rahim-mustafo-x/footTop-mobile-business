package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface LoginResult {
    data object Success : LoginResult
    data object RegisterRequired : LoginResult
    data object InvalidOtp : LoginResult
}

interface AuthRepository {
    fun sendOtp(phoneNumber: String): Flow<Boolean>
    fun login(phoneNumber: String, otp: String): Flow<LoginResult>
    fun isLoginIn(): Flow<Boolean>
    suspend fun logout()
}