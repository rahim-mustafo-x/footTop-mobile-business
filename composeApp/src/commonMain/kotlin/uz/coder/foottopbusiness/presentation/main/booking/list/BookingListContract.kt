package uz.coder.foottopbusiness.presentation.main.booking.list

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

sealed interface BookingListContract {
    data class State(
        val bookings: List<BookingResponseDto> = emptyList(),
        val isLoading: Boolean = false,
        val showCancelDialog: Boolean = false,
        val cancelReason: String = "",
        val bookingToCancel: Long? = null,
        val startDate: String? = null,
        val endDate: String? = null,
        val stadiumId: Long? = null
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        object NavigateBack : Effect
    }

    sealed interface Event : MviEvent {
        object Refresh : Event
        object BackClick : Event
        data class OpenCancelDialog(val bookingId: Long) : Event
        object DismissCancelDialog : Event
        data class UpdateCancelReason(val reason: String) : Event
        data class ConfirmCancelBooking(val bookingId: Long, val reason: String) : Event
        data class FilterByDate(val start: String, val end: String) : Event
    }
}
