package uz.coder.foottopbusiness.data.repository

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
        response.data?.token?.let {
            preferencesManager.setToken(it)
        }
        emit(response.success?:false)
    }
}