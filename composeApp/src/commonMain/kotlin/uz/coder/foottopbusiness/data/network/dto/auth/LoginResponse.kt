package uz.coder.foottopbusiness.data.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("accessToken")
    val token: String? = null,
    @SerialName("id")
    val userId: Int? = null
)