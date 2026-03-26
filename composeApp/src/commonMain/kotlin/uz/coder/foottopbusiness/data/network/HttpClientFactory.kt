package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.local.PreferencesManager

class HttpClientFactory(
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager
) {
    companion object {
        const val BASE_URL = "http://83.222.19.225:5002"
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    fun create(): HttpClient {
        val client = HttpClient {
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

                    // 401 va 403 statuslarini token eskirgan yoki yaroqsiz deb hisoblaymiz
                    if ((code == 401 || code == 403) && !isAuthEndpoint) {
                        log("Auth", "Session expired or Forbidden ($code) for $path. Redirecting to login.")
                        scope.launch {
                            // Mahalliy saqlangan ma'lumotlarni tozalaymiz
                            preferencesManager.logout()
                            // Navigatsiyani login sahifasiga o'tkazamiz
                            sessionManager.onUnauthorized()
                        }
                    }
                }
            }
        }

        /**
         * Ktor'ning standard 'Auth' plagini o'rniga manual interceptor ishlatamiz.
         * Sababi: 'Auth' plagini loadTokens natijasini keshlab oladi va login/logout'dan keyin
         * yangi tokenni har doim ham darhol ilib olmaydi (caching issue).
         * Bu interceptor esa har bir so'rovda eng yangi tokenni PreferencesManager'dan oladi.
         */
        client.plugin(HttpSend).intercept { request ->
            val path = request.url.encodedPath
            val isAuthEndpoint = path.contains("/api/auth/") || path.contains("/login") || path.contains("/send-otp")
            
            if (!isAuthEndpoint) {
                // PreferencesManager'dan joriy tokenni olamiz
                val token = preferencesManager.token.firstOrNull()
                if (!token.isNullOrEmpty()) {
                    // Header qo'shish (normalizeBearerToken allaqachon preferences'da tozalangan bo'ladi)
                    request.header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            
            execute(request)
        }

        return client
    }
}
