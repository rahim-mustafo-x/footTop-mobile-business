package uz.coder.foottopbusiness.data.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerialName("accessToken")
    val accessToken: String? = null,
    @SerialName("refreshToken")
    val refreshToken: String? = null,
    @SerialName("token")
    val token: String? = null,
    @SerialName("accessTokenExpiresIn")
    val accessTokenExpiresIn: Long? = null,
    @SerialName("expiresIn")
    val expiresIn: Long? = null,
    @SerialName("refreshTokenExpiresIn")
    val refreshTokenExpiresIn: Long? = null,
    @SerialName("refreshExpiresIn")
    val refreshExpiresIn: Long? = null,
) {
    fun resolvedAccessToken(): String? = accessToken ?: token

    fun resolvedAccessTokenExpiresIn(): Long? = accessTokenExpiresIn ?: expiresIn
    fun resolvedRefreshTokenExpiresIn(): Long? = refreshTokenExpiresIn ?: refreshExpiresIn
}
