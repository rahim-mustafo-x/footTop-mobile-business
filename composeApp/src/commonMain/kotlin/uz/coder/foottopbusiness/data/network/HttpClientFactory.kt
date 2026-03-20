package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
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

object HttpClientFactory {
    const val BASE_URL = "http://83.222.19.225:5002"

    fun create(preferencesManager: PreferencesManager, sessionManager: SessionManager): HttpClient {
        val scope = CoroutineScope(Dispatchers.Default)
        // token o'zgarishlarini doimiy kuzatib turadi — login/logout da avtomatik yangilanadi
        val tokenFlow = preferencesManager.token.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        log("Ktor", message)
                    }
                }
                level = LogLevel.BODY
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
                    val isAuthEndpoint = path.contains("/login") || path.contains("/send-otp")
                    if ((code == 401 || code == 403) && !isAuthEndpoint) {
                        val currentToken = tokenFlow.value
                        val requestToken = response.call.request.headers["Authorization"]
                            ?.removePrefix("Bearer ")?.trim()
                        if (currentToken != null && currentToken == requestToken) {
                            log("Auth", "Session expired ($code) for $path — clearing session")
                            sessionManager.onUnauthorized()
                        } else {
                            log("Auth", "Ignored $code for $path — token mismatch or already cleared")
                        }
                    }
                }
            }
        }
    }
}
