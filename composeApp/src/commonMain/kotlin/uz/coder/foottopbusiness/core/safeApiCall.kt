package uz.coder.foottopbusiness.core

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

suspend inline fun <reified T> safeApiCall(block: suspend () -> HttpResponse): Result<T> {
    return try {
        val response = block()
        if (response.status.isSuccess()) {
            Result.success(response.body<T>())
        } else {
            Result.failure(Exception("HTTP ${response.status.value}: ${response.bodyAsText()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// token bilan — bearerAuth ni builder ga qo'shadi
fun HttpRequestBuilder.applyToken(token: String?) {
    token?.let { bearerAuth(it) }
}
