package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.local.PreferencesManager

class HttpClientFactory(
    preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager
) {
    companion object {
        const val BASE_URL = "http://83.222.19.225:5002"
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val tokenState = preferencesManager.token.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        // Access the latest value immediately from StateFlow
                        val token = tokenState.value
                        if (token.isNullOrEmpty()) {
                            log("Auth", "No token in StateFlow")
                            null
                        } else {
                            log("Auth", "Token loaded from StateFlow: ${token.take(10)}...")
                            BearerTokens(accessToken = token, refreshToken = "")
                        }
                    }
                    sendWithoutRequest { request ->
                        val path = request.url.encodedPath
                        !path.contains("/api/auth/") && !path.contains("/login") && !path.contains("/send-otp")
                    }
                }
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

            expectSuccess = false

            HttpResponseValidator {
                validateResponse { response ->
                    val code = response.status.value
                    val path = response.call.request.url.encodedPath
                    val isAuthEndpoint = path.contains("/api/auth/") || path.contains("/login") || path.contains("/send-otp")

                    if ((code == 401 || code == 403) && !isAuthEndpoint) {
                        log("Auth", "Session expired or Forbidden ($code) for $path")
                        sessionManager.onUnauthorized()
                    }
                }
            }
        }
    }
}
