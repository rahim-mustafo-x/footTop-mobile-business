package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.log
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
    }.catch {
        log("NotificationRepository", "sendNotification error: ${it.message}")
        emit(false)
    }

    override fun sendToAll(request: NotificationRequest) = flow {
        val response = apiService.sendToAll(request)
        emit(response.success ?: false)
    }.catch {
        log("NotificationRepository", "sendToAll error: ${it.message}")
        emit(false)
    }

    override fun registerDeviceToken(request: DeviceTokenRequest) = flow {
        val response = apiService.registerDeviceToken(request)
        emit(response.success ?: false)
    }.catch {
        log("NotificationRepository", "registerDeviceToken error: ${it.message}")
        emit(false)
    }

    override fun removeDeviceToken(token: String) = flow {
        val response = apiService.removeDeviceToken(token)
        emit(response.success ?: false)
    }.catch {
        log("NotificationRepository", "removeDeviceToken error: ${it.message}")
        emit(false)
    }

    override fun getMyNotifications(userId: Long) = flow {
        val response = apiService.getMyNotifications(userId)
        emit(response.data ?: emptyList())
    }.catch {
        log("NotificationRepository", "getMyNotifications error: ${it.message}")
        emit(emptyList<NotificationResponse>())
    }

    override fun getUnreadCount(userId: Long) = flow {
        val response = apiService.getUnreadCount(userId)
        emit(response.data ?: 0)
    }.catch {
        log("NotificationRepository", "getUnreadCount error: ${it.message}")
        emit(0)
    }

    override fun markAsRead(notificationId: Long) = flow {
        val response = apiService.markAsRead(notificationId)
        emit(response.success ?: false)
    }.catch {
        log("NotificationRepository", "markAsRead error: ${it.message}")
        emit(false)
    }

    override fun markAllAsRead(userId: Long) = flow {
        val response = apiService.markAllAsRead(userId)
        emit(response.success ?: false)
    }.catch {
        log("NotificationRepository", "markAllAsRead error: ${it.message}")
        emit(false)
    }
}
