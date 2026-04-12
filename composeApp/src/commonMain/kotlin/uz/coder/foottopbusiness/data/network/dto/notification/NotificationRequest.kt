package uz.coder.foottopbusiness.data.network.dto.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("type")
    val type: String = "SYSTEM",
    @SerialName("targetType")
    val targetType: String,
    @SerialName("targetUserId")
    val targetUserId: Long? = null,
    @SerialName("senderId")
    val senderId: Long? = null
)
