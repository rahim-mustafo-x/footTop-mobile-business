package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.data.network.dto.auth.LoginStatus
import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.repository.LoginResult

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
) : AuthRepository {

    override fun sendOtp(phoneNumber: String) = flow {
        val response = authApiService.sendOtp(phoneNumber)
        emit(response.success ?: false)
    }

    override fun login(phoneNumber: String, otp: String) = flow {
        val response = authApiService.login(phoneNumber, otp)
        when (response.status) {
            LoginStatus.REGISTER_REQUIRED -> {
                emit(LoginResult.RegisterRequired)
            }
            LoginStatus.INVALID_OTP -> {
                emit(LoginResult.InvalidOtp)
            }
            else -> {
                // status null — muvaffaqiyatli login
                response.token?.let {
                    preferencesManager.setToken(it)
                    sessionManager.onAuthorized()
                }
                response.userId?.let {
                    preferencesManager.setAuthorised(true)
                    preferencesManager.setUserId(it)
                }
                emit(LoginResult.Success)
            }
        }
    }

    override fun isLoginIn(): Flow<Boolean> = preferencesManager.authorised
}
