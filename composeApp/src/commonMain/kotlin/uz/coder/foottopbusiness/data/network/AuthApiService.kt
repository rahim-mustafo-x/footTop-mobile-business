package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.auth.LoginRequest
import uz.coder.foottopbusiness.data.network.dto.auth.LoginResponse
import uz.coder.foottopbusiness.data.network.dto.auth.SendOtpResponse

class AuthApiService(private val client: HttpClient) {
    companion object{
        private const val SEND_OTP_END_POINT = "/api/auth/send-otp"
        private const val LOGIN_END_POINT = "/api/auth/login"
    }
    suspend fun sendOtp(phoneNumber: String) = client.post(HttpClientFactory.BASE_URL+SEND_OTP_END_POINT) {
        url{
            parameters.append("phoneNumber", "+998$phoneNumber")
        }
        contentType(ContentType.Application.Json)
    }.body<BaseResponse<SendOtpResponse>>()
    suspend fun login(phoneNumber: String, otpCode: String) = client.post(HttpClientFactory.BASE_URL+LOGIN_END_POINT){
        setBody(LoginRequest(phoneNumber, otpCode))
        contentType(ContentType.Application.Json)
    }.body<BaseResponse<LoginResponse>>()
}