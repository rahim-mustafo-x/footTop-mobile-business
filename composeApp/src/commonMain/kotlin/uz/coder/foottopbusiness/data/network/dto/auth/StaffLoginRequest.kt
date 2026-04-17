package uz.coder.foottopbusiness.data.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaffLoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)
