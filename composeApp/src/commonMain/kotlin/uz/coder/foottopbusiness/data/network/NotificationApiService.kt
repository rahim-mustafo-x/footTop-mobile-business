package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.data.network.dto.notification.NotificationRequest

class NotificationApiService(private val client: HttpClient) {

    // Admin/Owner - Send to specific user
    suspend fun sendNotification(request: NotificationRequest): BaseResponse<Unit> {
        return client.post("/v1/notifications/send") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Admin/Owner - Send to all users
    suspend fun sendToAll(request: NotificationRequest): BaseResponse<Unit> {
        return client.post("/v1/notifications/send-to-all") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Device Token Registration
    suspend fun registerDeviceToken(request: DeviceTokenRequest): BaseResponse<Unit> {
        return client.post("/v1/notifications/device-token/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Device Token Removal (on Logout)
    suspend fun removeDeviceToken(token: String): BaseResponse<Unit> {
        return client.delete("/v1/notifications/device-token") {
            url {
                parameters.append("token", token)
            }
        }.body()
    }

    // Get My Notifications
    suspend fun getMyNotifications(userId: Long): BaseResponse<List<NotificationResponse>> {
        return client.get("/v1/notifications/my/$userId").body()
    }

    // Get Unread Count
    suspend fun getUnreadCount(userId: Long): BaseResponse<Int> {
        return client.get("/v1/notifications/unread-count/$userId").body()
    }

    // Mark as Read
    suspend fun markAsRead(notificationId: Long): BaseResponse<Unit> {
        return client.put("/v1/notifications/$notificationId/read").body()
    }

    // Mark All as Read
    suspend fun markAllAsRead(userId: Long): BaseResponse<Unit> {
        return client.put("/v1/notifications/read-all/$userId").body()
    }
}

@kotlinx.serialization.Serializable
data class NotificationResponse(
    val id: Long,
    val title: String,
    val body: String,
    val type: String,
    val targetType: String,
    val isRead: Boolean,
    val sentAt: String,
    val readAt: String? = null
)
