package uz.coder.foottopbusiness.data.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("phoneNumber")
    val phoneNumber: String,
    @SerialName("otpCode")
    val otpCode: String
)