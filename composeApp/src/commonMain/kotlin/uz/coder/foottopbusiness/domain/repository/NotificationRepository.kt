package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.NotificationResponse
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.data.network.dto.notification.NotificationRequest

interface NotificationRepository {
    fun sendNotification(request: NotificationRequest): Flow<Boolean>
    fun sendToAll(request: NotificationRequest): Flow<Boolean>
    fun registerDeviceToken(request: DeviceTokenRequest): Flow<Boolean>
    fun removeDeviceToken(token: String): Flow<Boolean>
    fun getMyNotifications(userId: Long): Flow<List<NotificationResponse>>
    fun getUnreadCount(userId: Long): Flow<Int>
    fun markAsRead(notificationId: Long): Flow<Boolean>
    fun markAllAsRead(userId: Long): Flow<Boolean>
}
