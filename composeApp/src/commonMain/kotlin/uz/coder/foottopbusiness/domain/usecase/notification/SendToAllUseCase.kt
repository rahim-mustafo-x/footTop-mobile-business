package uz.coder.foottopbusiness.domain.usecase.notification

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.notification.NotificationRequest
import uz.coder.foottopbusiness.domain.repository.NotificationRepository

class SendToAllUseCase(private val repository: NotificationRepository) {
    operator fun invoke(request: NotificationRequest): Flow<Boolean> =
        repository.sendToAll(request)
}
