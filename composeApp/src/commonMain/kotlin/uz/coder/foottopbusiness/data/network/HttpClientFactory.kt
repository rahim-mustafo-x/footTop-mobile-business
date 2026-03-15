package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.local.PreferencesManager

object HttpClientFactory {
    const val BASE_URL = "http://83.222.19.225:5002"
    private const val TAG = "HttpClientFactory"

    fun create(preferencesManager: PreferencesManager) = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging){
            logger = object : Logger {
                override fun log(message: String) {
                    log("Ktor", message)
                }
            }
            level = LogLevel.BODY
        }
        install(Auth){
            bearer {
                loadTokens {
                    log(TAG, "Loading tokens...")
                    val token = preferencesManager.token.firstOrNull()
                    log(TAG, "Token loaded: $token")
                    token?.let { BearerTokens(it, "") }
                }
                sendWithoutRequest { request ->
                    val encodedPath = request.url.encodedPath
                    // Viloyat, tuman, login va otp uchun tokenni talab qilmaslik
                    val skipAuth = encodedPath.contains("/login") || 
                                 encodedPath.contains("/send-otp") ||
                                 encodedPath.contains("/regions") ||
                                 encodedPath.contains("/districts-by-region")

                    log(TAG, "Sending without request for $encodedPath: $skipAuth")
                    skipAuth
                }
            }
        }
        install(HttpTimeout){
            requestTimeoutMillis = 30000L
            connectTimeoutMillis = 30000L
            socketTimeoutMillis = 30000L
        }
    }
}
