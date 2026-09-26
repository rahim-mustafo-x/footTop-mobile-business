package uz.coder.foottopbusiness.presentation.main.booking.details

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

sealed interface BookingDetailsContract {
    data class State(
        val booking: BookingResponseDto,
        /** Tasdiqlash/rad etish so'rovi ketayotganda tugmalar bloklanadi. */
        val isProcessing: Boolean = false,
        val showRejectDialog: Boolean = false,
        val rejectReason: String = "",
        /** Rad etish dialogi butun takroriy seriya uchun ochilganmi. */
        val rejectWholeSeries: Boolean = false,
        val showCancelSeriesDialog: Boolean = false,
        val cancelSeriesReason: String = ""
    ) : MviState {
        val isRecurring: Boolean get() = !booking.recurrenceGroupId.isNullOrBlank()
    }

    sealed interface Effect : MviEffect {
        /** Xom xabar - ekranda ErrorMapper orqali foydalanuvchi tiliga o'giriladi. */
        data class ShowError(val message: String) : Effect
        object BookingConfirmed : Effect
        object BookingRejected : Effect
        object PaymentStatusUpdated : Effect
        object SeriesUpdated : Effect
    }

    sealed interface Event : MviEvent {
        object Confirm : Event
        object OpenRejectDialog : Event
        object DismissRejectDialog : Event
        data class UpdateRejectReason(val reason: String) : Event
        object SubmitReject : Event

        /** PAID <-> UNPAID. */
        object TogglePaymentStatus : Event

        object ConfirmSeries : Event
        object OpenRejectSeriesDialog : Event
        object OpenCancelSeriesDialog : Event
        object DismissCancelSeriesDialog : Event
        data class UpdateCancelSeriesReason(val reason: String) : Event
        object SubmitCancelSeries : Event
    }
}
