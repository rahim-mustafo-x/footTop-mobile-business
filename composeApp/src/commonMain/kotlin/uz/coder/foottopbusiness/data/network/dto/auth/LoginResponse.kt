package uz.coder.foottopbusiness.data.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("accessToken")
    val accessToken: String? = null,
    /** Ba'zi serverlar faqat `token` maydonini qaytaradi */
    @SerialName("token")
    val token: String? = null,
    @SerialName("refreshToken")
    val refreshToken: String? = null,
    @SerialName("accessTokenExpiresIn")
    val accessTokenExpiresIn: Long? = null,
    @SerialName("expiresIn")
    val expiresIn: Long? = null,
    @SerialName("refreshTokenExpiresIn")
    val refreshTokenExpiresIn: Long? = null,
    @SerialName("refreshExpiresIn")
    val refreshExpiresIn: Long? = null,
    @SerialName("id")
    val userId: Int? = null,
    @SerialName("status")
    val status: String? = null,
) {
    fun resolvedAccessToken(): String? = accessToken ?: token

    fun resolvedAccessTokenExpiresIn(): Long? = accessTokenExpiresIn ?: expiresIn
    fun resolvedRefreshTokenExpiresIn(): Long? = refreshTokenExpiresIn ?: refreshExpiresIn
}

// status qiymatlari
object LoginStatus {
    const val REGISTER_REQUIRED = "REGISTER_REQUIRED"
    const val INVALID_OTP = "INVALID_OTP"
}