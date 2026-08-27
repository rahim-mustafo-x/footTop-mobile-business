package uz.coder.foottopbusiness.presentation.main.booking.list

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

sealed interface BookingListContract {
    data class State(
        val bookings: List<BookingResponseDto> = emptyList(),
        val filteredBookings: List<BookingResponseDto> = emptyList(),
        val selectedTab: Int = 0, // 0: All, 1: Upcoming, 2: Active, 3: Completed, 4: Cancelled, 5: Pending
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val showCancelDialog: Boolean = false,
        val cancelReason: String = "",
        val bookingToCancel: Long? = null,
        val showRejectDialog: Boolean = false,
        val rejectReason: String = "",
        val bookingToReject: Long? = null,
        /** Tasdiqlash/rad etish davom etayotgan bron -- tugmalarni bloklash uchun. */
        val processingBookingId: Long? = null,
        val page: Int = 0,
        val canLoadMore: Boolean = true,
        val isLoadingMore: Boolean = false,
        val startDate: String? = null,
        val endDate: String? = null,
        val stadiumId: Long? = null
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        object NavigateBack : Effect
        data class NavigateToDetails(val booking: BookingResponseDto) : Effect
        object BookingConfirmed : Effect
        object BookingRejected : Effect
    }

    sealed interface Event : MviEvent {
        object Refresh : Event
        object BackClick : Event
        data class ChangeTab(val index: Int) : Event
        data class SelectBooking(val booking: BookingResponseDto) : Event
        data class OpenCancelDialog(val bookingId: Long) : Event
        object DismissCancelDialog : Event
        data class UpdateCancelReason(val reason: String) : Event
        data class ConfirmCancelBooking(val bookingId: Long, val reason: String) : Event
        data class FilterByDate(val start: String, val end: String) : Event

        data class ConfirmBooking(val bookingId: Long) : Event
        data class OpenRejectDialog(val bookingId: Long) : Event
        object DismissRejectDialog : Event
        data class UpdateRejectReason(val reason: String) : Event
        data class SubmitReject(val bookingId: Long, val reason: String) : Event
        object LoadMore : Event
    }
}
