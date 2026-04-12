package uz.coder.foottopbusiness.data.network.dto.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRequest(
    @SerialName("userId")
    val userId: Long,
    @SerialName("token")
    val token: String,
    @SerialName("deviceType")
    val deviceType: String // ANDROID, IOS, WEB
)
