package uz.coder.foottopbusiness.domain.usecase.notification

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.domain.repository.NotificationRepository

class RegisterDeviceTokenUseCase(private val repository: NotificationRepository) {
    operator fun invoke(request: DeviceTokenRequest): Flow<Boolean> =
        repository.registerDeviceToken(request)
}
