package uz.coder.foottopbusiness.presentation.main.settings.notification

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.notification.NotificationRequest
import uz.coder.foottopbusiness.domain.usecase.notification.SendNotificationUseCase

import uz.coder.foottopbusiness.domain.usecase.notification.SendToAllUseCase

class SendNotificationViewModel(
    private val sendToAllUseCase: SendToAllUseCase
) : BaseViewModel<SendNotificationContract.State, SendNotificationContract.Effect, SendNotificationContract.Event>(
    initialState = SendNotificationContract.State()
) {
    override fun handleEvent(event: SendNotificationContract.Event) {
        when (event) {
            is SendNotificationContract.Event.UpdateTitle -> updateState { copy(title = event.title) }
            is SendNotificationContract.Event.UpdateBody -> updateState { copy(body = event.body) }
            SendNotificationContract.Event.SendToAll -> sendNotification()
            SendNotificationContract.Event.ResetSuccess -> updateState { copy(isSuccess = false) }
        }
    }

    private fun sendNotification() {
        val title = state.value.title
        val body = state.value.body

        if (title.isBlank() || body.isBlank()) {
            sendEffect(SendNotificationContract.Effect.ShowToast("Iltimos, barcha maydonlarni to'ldiring"))
            return
        }

        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            onError = {
                updateState { copy(isLoading = false) }
                sendEffect(SendNotificationContract.Effect.ShowToast(it.message ?: "Xatolik yuz berdi"))
            },
            onSuccess = { success ->
                updateState { copy(isLoading = false) }
                if (success) {
                    updateState { copy(isSuccess = true, title = "", body = "") }
                    sendEffect(SendNotificationContract.Effect.ShowToast("Xabarnoma yuborildi"))
                } else {
                    sendEffect(SendNotificationContract.Effect.ShowToast("Xabarnoma yuborishda xatolik"))
                }
            }
        ) {
            val request = NotificationRequest(
                title = title,
                body = body,
                type = "SYSTEM",
                targetType = "ALL"
            )
            sendToAllUseCase(request).first()
        }
    }
}
