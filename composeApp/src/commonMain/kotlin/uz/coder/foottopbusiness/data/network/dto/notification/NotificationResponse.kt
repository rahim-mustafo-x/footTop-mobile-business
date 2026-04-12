package uz.coder.foottopbusiness.data.network.dto.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("type")
    val type: String,
    @SerialName("targetType")
    val targetType: String,
    @SerialName("isRead")
    val isRead: Boolean,
    @SerialName("sentAt")
    val sentAt: String,
    @SerialName("readAt")
    val readAt: String? = null
)
