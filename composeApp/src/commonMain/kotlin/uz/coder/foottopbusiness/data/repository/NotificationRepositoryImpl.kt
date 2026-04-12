package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.NotificationApiService
import uz.coder.foottopbusiness.data.network.NotificationResponse
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.data.network.dto.notification.NotificationRequest
import uz.coder.foottopbusiness.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val apiService: NotificationApiService
) : NotificationRepository {

    override fun sendNotification(request: NotificationRequest) = flow {
        val response = apiService.sendNotification(request)
        emit(response.success ?: false)
    }

    override fun sendToAll(request: NotificationRequest) = flow {
        val response = apiService.sendToAll(request)
        emit(response.success ?: false)
    }

    override fun registerDeviceToken(request: DeviceTokenRequest) = flow {
        val response = apiService.registerDeviceToken(request)
        emit(response.success ?: false)
    }

    override fun removeDeviceToken(token: String) = flow {
        val response = apiService.removeDeviceToken(token)
        emit(response.success ?: false)
    }

    override fun getMyNotifications(userId: Long) = flow {
        val response = apiService.getMyNotifications(userId)
        emit(response.data ?: emptyList())
    }

    override fun getUnreadCount(userId: Long) = flow {
        val response = apiService.getUnreadCount(userId)
        emit(response.data ?: 0)
    }

    override fun markAsRead(notificationId: Long) = flow {
        val response = apiService.markAsRead(notificationId)
        emit(response.success ?: false)
    }

    override fun markAllAsRead(userId: Long) = flow {
        val response = apiService.markAllAsRead(userId)
        emit(response.success ?: false)
    }
}
