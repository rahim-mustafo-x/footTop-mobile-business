package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.normalizeBearerToken
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.repository.LoginResult
import kotlin.time.Clock

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
) : AuthRepository {

    override fun staffLogin(username: String, password: String) = flow {
        val response = authApiService.staffLogin(uz.coder.foottopbusiness.data.network.dto.auth.StaffLoginRequest(username, password))
        val access = normalizeBearerToken(response.accessToken)
        val refresh = normalizeBearerToken(response.refreshToken)
        if (access != null && refresh != null) {
            saveAuthData(
                accessToken = access,
                refreshToken = refresh,
                accessExpiresIn = response.accessTokenExpiresIn,
                refreshExpiresIn = response.refreshTokenExpiresIn,
                userId = response.id,
                role = response.roles?.firstOrNull()
            )
            emit(LoginResult.Success)
        } else {
            emit(LoginResult.InvalidOtp)
        }
    }

    private suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        accessExpiresIn: Long?,
        refreshExpiresIn: Long?,
        userId: Long?,
        role: String?
    ) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        
        // Update SessionManager immediately to avoid race conditions with next requests
        sessionManager.setToken(accessToken)

        preferencesManager.setToken(accessToken)
        preferencesManager.setRefreshToken(refreshToken)
        
        val aExpires = accessExpiresIn ?: 3600L
        val rExpires = refreshExpiresIn ?: 86400L
        
        preferencesManager.setAccessTokenExpiration(currentTime + aExpires * 1000L)
        preferencesManager.setRefreshTokenExpiration(currentTime + rExpires * 1000L)
        
        preferencesManager.setAuthorised(true)
        sessionManager.onAuthorized()
        
        userId?.let {
            preferencesManager.setUserId(it.toInt())
        }
        role?.let {
            preferencesManager.setRole(it)
        }
    }

    override fun isLoginIn(): Flow<Boolean> = preferencesManager.authorised

    override suspend fun logout() {
        sessionManager.logout()
    }

    override fun changePassword(oldPassword: String, newPassword: String) = flow {
        authApiService.changePassword(
            uz.coder.foottopbusiness.data.network.dto.auth.ChangePasswordRequest(
                oldPassword = oldPassword,
                newPassword = newPassword
            )
        )
        emit(Unit)
    }
}
