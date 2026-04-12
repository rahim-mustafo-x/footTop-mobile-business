package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.normalizeBearerToken
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.data.network.dto.auth.LoginStatus
import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.repository.LoginResult
import kotlin.time.Clock

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
                val access = normalizeBearerToken(response.resolvedAccessToken())
                val refreshToken = normalizeBearerToken(response.refreshToken)
                if (access == null || refreshToken == null) {
                    emit(LoginResult.InvalidOtp)
                    return@flow
                }

                // 1. Save tokens
                val currentTime = Clock.System.now().toEpochMilliseconds()
                preferencesManager.setToken(access)
                preferencesManager.setRefreshToken(refreshToken)
                val accessExpiresIn = response.resolvedAccessTokenExpiresIn() ?: 900L
                val refreshExpiresIn = response.resolvedRefreshTokenExpiresIn() ?: 900L
                preferencesManager.setAccessTokenExpiration(currentTime + accessExpiresIn * 1000L)
                preferencesManager.setRefreshTokenExpiration(currentTime + refreshExpiresIn * 1000L)
                
                // 2. Mark as authorised immediately since we have a token
                preferencesManager.setAuthorised(true)
                
                // 3. Reset session state
                sessionManager.onAuthorized()
                
                // 4. Save User ID if available
                response.userId?.let {
                    preferencesManager.setUserId(it)
                }

                emit(LoginResult.Success)
            }
        }
    }

    override fun isLoginIn(): Flow<Boolean> = preferencesManager.authorised

    override suspend fun logout() {
        preferencesManager.logout()
        sessionManager.onUnauthorized()
    }
}
