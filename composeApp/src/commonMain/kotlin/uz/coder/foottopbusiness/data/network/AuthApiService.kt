package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.auth.RefreshTokenRequest
import uz.coder.foottopbusiness.data.network.dto.auth.StaffLoginRequest
import uz.coder.foottopbusiness.data.network.dto.auth.StaffLoginResponse
import uz.coder.foottopbusiness.data.network.dto.auth.TokenResponse

class AuthApiService(private val client: HttpClient) {
    companion object{
        private const val STAFF_LOGIN_END_POINT = "/api/auth/staff/login"
        private const val REFRESH_END_POINT = "/api/auth/refresh"
        private const val LOGOUT_END_POINT = "/api/auth/logout"
        private const val CHANGE_PASSWORD_END_POINT = "/api/auth/change-password"
    }

    suspend fun changePassword(request: uz.coder.foottopbusiness.data.network.dto.auth.ChangePasswordRequest) = client.post(CHANGE_PASSWORD_END_POINT) {
        setBody(request)
        contentType(ContentType.Application.Json)
    }.body<BaseResponse<uz.coder.foottopbusiness.data.network.dto.EmptyData>>()

    suspend fun staffLogin(request: StaffLoginRequest) = client.post(STAFF_LOGIN_END_POINT) {
        setBody(request)
        contentType(ContentType.Application.Json)
    }.body<StaffLoginResponse>()

    suspend fun refreshToken(refreshToken: String) = client.post(REFRESH_END_POINT) {
        setBody(RefreshTokenRequest(refreshToken))
        contentType(ContentType.Application.Json)
    }.body<TokenResponse>()

    suspend fun logout(fcmToken: String) = client.post(LOGOUT_END_POINT) {
        url {
            parameters.append("fcmToken", fcmToken)
        }
        contentType(ContentType.Application.Json)
    }.body<BaseResponse<String>>()
}