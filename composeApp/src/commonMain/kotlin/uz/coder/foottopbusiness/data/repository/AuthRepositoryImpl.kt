package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val preferencesManager: PreferencesManager
): AuthRepository {
    override fun sendOtp(phoneNumber: String) = flow {
        val response = authApiService.sendOtp(phoneNumber)
        emit(response.success?:false)
    }

    override fun login(
        phoneNumber: String,
        otp: String
    ) = flow {
        val response = authApiService.login(phoneNumber, otp)
        response.token?.let {
            preferencesManager.setToken(it)
            preferencesManager.setAuthorised(true)
        }
        emit(response.token?.isNotBlank()?:false)
    }

    override fun isLoginIn(): Flow<Boolean> {
        return preferencesManager.authorised
    }
}