package uz.coder.foottopbusiness.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.core.NetworkConfig.BASE_URL
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.auth.RefreshTokenRequest
import uz.coder.foottopbusiness.data.network.dto.auth.TokenResponse
import kotlin.time.Clock

class SessionManager(private val preferencesManager: PreferencesManager) {
    sealed class SessionEvent {
        data object Authenticated : SessionEvent()
        data object Logout : SessionEvent()
    }

    private val refreshMutex = Mutex()
    private var isObservingToken = false

    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()

    private val _networkError = MutableSharedFlow<Int>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val networkError = _networkError.asSharedFlow()

    val isTokenValid: Flow<Boolean> = token.map { !it.isNullOrBlank() }

    private val _sessionEvent = MutableSharedFlow<SessionEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun setToken(newToken: String?) {
        _token.value = newToken
    }

    fun emitNetworkError(code: Int) {
        _networkError.tryEmit(code)
    }

    fun emitEvent(event: SessionEvent) {
        _sessionEvent.tryEmit(event)
    }

    fun onAuthorized() {
        emitEvent(SessionEvent.Authenticated)
    }

    fun onUnauthorized() {
        emitEvent(SessionEvent.Logout)
    }

    private fun log(tag: String, message: String) {
        println("[$tag] $message")
    }

    suspend fun logout() {
        preferencesManager.logout()
        setToken(null)
        onUnauthorized()
    }

    fun startObservingToken(scope: kotlinx.coroutines.CoroutineScope) {
        observeToken(scope)
    }

    fun observeToken(scope: kotlinx.coroutines.CoroutineScope) {
        if (isObservingToken) return
        isObservingToken = true
        scope.launch {
            preferencesManager.token.collect { token ->
                setToken(token)
            }
        }
    }

    suspend fun refreshToken(
        refreshToken: String
    ): BearerTokens? = refreshMutex.withLock {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val currentAccessToken = preferencesManager.token.first()
        val expiration = preferencesManager.accessTokenExpiration.first()
        val refreshExpiration = preferencesManager.refreshTokenExpiration.first()

        if (refreshExpiration in 1..currentTime) {
            log("Auth", "Refresh token expired locally, logging out")
            logout()
            return@withLock null
        }
        
        if (!currentAccessToken.isNullOrBlank() && expiration > currentTime + 10000) {
            val latestRefresh = preferencesManager.refreshToken.first() ?: ""
            return@withLock BearerTokens(currentAccessToken, latestRefresh)
        }

        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        log("Ktor-Refresh", message)
                    }
                }
                level = LogLevel.ALL
            }
        }

        return try {
            log("Auth", "Attempting to refresh token...")
            val response: TokenResponse = client.post("$BASE_URL/api/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }.body()

            val newAccess = response.accessToken ?: response.token
            val newRefresh = response.refreshToken

            if (!newAccess.isNullOrBlank() && !newRefresh.isNullOrBlank()) {
                val newCurrentTime = Clock.System.now().toEpochMilliseconds()
                
                preferencesManager.setToken(newAccess)
                preferencesManager.setRefreshToken(newRefresh)
                
                val accessExpireAt = newCurrentTime + (response.resolvedAccessTokenExpiresIn() ?: 900L) * 1000L
                val refreshExpireAt = newCurrentTime + (response.resolvedRefreshTokenExpiresIn() ?: 2592000L) * 1000L

                preferencesManager.setAccessTokenExpiration(accessExpireAt)
                preferencesManager.setRefreshTokenExpiration(refreshExpireAt)
                
                setToken(newAccess)
                log("Auth", "Token refreshed successfully. Expires at: $accessExpireAt")
                BearerTokens(newAccess, newRefresh)
            } else {
                log("Auth", "Refresh response invalid: access or refresh token is null")
                null
            }
        } catch (e: Exception) {
            log("Auth", "Refresh failed with exception: ${e.message}")
            if (e is io.ktor.client.plugins.ResponseException) {
                if (e.response.status.value == 401 || e.response.status.value == 403 || e.response.status.value == 500) {
                    log("Auth", "Refresh token is likely invalid/expired (${e.response.status.value}), logging out")
                    emitEvent(SessionEvent.Logout)
                    preferencesManager.logout()
                }
            }
            null
        } finally {
            client.close()
        }
    }
}
