package uz.coder.foottopbusiness.data.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaffLoginResponse(
    @SerialName("id") val id: Long? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("accessToken") val accessToken: String? = null,
    @SerialName("refreshToken") val refreshToken: String? = null,
    @SerialName("accessTokenExpiresIn") val accessTokenExpiresIn: Long? = null,
    @SerialName("refreshTokenExpiresIn") val refreshTokenExpiresIn: Long? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("fullName") val fullName: String? = null,
    @SerialName("profileImageUrl") val profileImageUrl: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("roles") val roles: List<String>? = null
)
