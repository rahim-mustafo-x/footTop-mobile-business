package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.data.local.PreferencesManager

object HttpClientFactory {
    const val BASE_URL = "http://83.222.19.225:5002"
    fun create(preferencesManager: PreferencesManager) = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging){
            level = LogLevel.BODY.also {  LogLevel.HEADERS }
        }
        install(Auth){
            bearer {
                loadTokens {
                    val token = preferencesManager.token.firstOrNull()
                    token?.let { BearerTokens(it, "") }
                }
                sendWithoutRequest { request ->
                    val encodedPath = request.url.encodedPath
                    encodedPath.contains("/login") || encodedPath.contains("/send-otp")
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