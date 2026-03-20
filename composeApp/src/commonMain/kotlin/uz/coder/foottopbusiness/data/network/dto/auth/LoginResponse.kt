package uz.coder.foottopbusiness.data.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("accessToken")
    val token: String? = null,
    @SerialName("id")
    val userId: Int? = null,
    @SerialName("status")
    val status: String? = null,
)

// status qiymatlari
object LoginStatus {
    const val REGISTER_REQUIRED = "REGISTER_REQUIRED"
    const val INVALID_OTP = "INVALID_OTP"
}