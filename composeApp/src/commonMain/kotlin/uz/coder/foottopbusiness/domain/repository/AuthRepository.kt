package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun sendOtp(phoneNumber: String): Flow<Boolean>
    fun login(phoneNumber: String, otp: String): Flow<Boolean>
    fun isLoginIn(): Flow<Boolean>
}