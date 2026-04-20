package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.BaseResponse

class HttpClientFactory(
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager
) {
    companion object {
        const val BASE_URL = "http://83.222.19.225:5002"
    }

    private val ioScope = CoroutineScope(Dispatchers.Default)

    fun create(): HttpClient {
        sessionManager.observeToken(ioScope)

        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    coerceInputValues = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        log("Ktor", message)
                    }
                }
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 30000L
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = preferencesManager.token.first()
                        val refreshToken = preferencesManager.refreshToken.first()
                        log("Auth", "loadTokens: ${accessToken?.take(10)}...")
                        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }
                    refreshTokens {
                        val refreshToken = preferencesManager.refreshToken.first()
                        log("Auth", "refreshTokens: ${refreshToken?.take(10)}...")
                        if (!refreshToken.isNullOrBlank()) {
                            sessionManager.refreshToken(refreshToken)
                        } else {
                            null
                        }
                    }
                    sendWithoutRequest { request ->
                        val path = request.url.encodedPath
                        !(path.contains("/api/auth/login") ||
                                path.contains("/api/auth/send-otp") ||
                                path.contains("/api/auth/refresh") ||
                                path.contains("/api/auth/logout") ||
                                path.contains("/v1/users/create") ||
                                request.url.host == "nominatim.openstreetmap.org")
                    }
                }
            }

            expectSuccess = false

            HttpResponseValidator {
                validateResponse { response ->
                    val code = response.status.value
                    val path = response.call.request.url.encodedPath
                    val isAuthEndpoint = path.contains("/api/auth/login") ||
                            path.contains("/api/auth/send-otp") ||
                            path.contains("/api/auth/refresh") ||
                            path.contains("/api/auth/logout")

                    if (!isAuthEndpoint) {
                        when (code) {
                            401, 403, 400, 404, 409 -> {
                                val errorBody = response.bodyAsText()
                                try {
                                    val baseResponse = response.body<BaseResponse<Unit>>()
                                    sessionManager.emitNetworkError(
                                        code = code,
                                        message = baseResponse.message,
                                        details = baseResponse.details
                                    )
                                } catch (e: Exception) {
                                    sessionManager.emitNetworkError(code)
                                }

                                if (code == 401 || code == 403) {
                                    if (errorBody.contains("TOKEN_EXPIRED", ignoreCase = true) || errorBody.contains("TOKEN", ignoreCase = true)) {
                                        log("Auth", "$code received with TOKEN error, attempting refresh")
                                        val refreshToken = preferencesManager.refreshToken.first()
                                        if (!refreshToken.isNullOrBlank()) {
                                            sessionManager.refreshToken(refreshToken)
                                        } else {
                                            ioScope.launch { sessionManager.logout() }
                                        }
                                    } else {
                                        log("Auth", "$code received without TOKEN error, logging out")
                                        ioScope.launch { sessionManager.logout() }
                                    }
                                }
                            }
                            500 -> {
                                log("Auth", "Received 500 on $path, logging out")
                                ioScope.launch {
                                    sessionManager.logout()
                                }
                                sessionManager.emitNetworkError(code)
                            }
                        }
                    }
                }
            }
            defaultRequest {
                url(BASE_URL)
            }
        }
    }
}
