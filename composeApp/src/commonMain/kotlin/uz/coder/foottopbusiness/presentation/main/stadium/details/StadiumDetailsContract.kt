package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.SlotDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.model.UserRole

sealed interface StadiumDetailsContract {
    companion object {
        fun durationMinutesKey(key: String): Int = when(key) {
            "SIXTY" -> 60
            "NINETY" -> 90
            "ONE_HUNDRED_TWENTY" -> 120
            else -> 60
        }
    }
    data class State(
        val stadium: StadiumResponse? = null,
        val stadiums: List<StadiumResponse> = emptyList(), // Support for multiple pitches
        val userRole: UserRole = UserRole.UNKNOWN,
        val isLoading: Boolean = false,
        val isUpdatingStatus: Boolean = false,
        val isBooking: Boolean = false,

        // Band qilingan slot tafsiloti - xodim slotni bosganda ko'rsatiladi
        val showBookingDetailsDialog: Boolean = false,
        val isLoadingBookingDetails: Boolean = false,
        // Slotga kesishgan bronlar. Bitta 60 daqiqalik slot ikkita bronga
        // to'g'ri kelishi mumkin, shuning uchun ro'yxat.
        val bookingDetails: List<BookingResponseDto> = emptyList(),

        // Selection state for booking logic
        val selectedDate: String? = null,
        val selectedDurationKey: String = "SIXTY",
        val selectedPitchIndex: Int? = null,
        val selectedStartIndex: Int? = null,
        val isSlotsLoading: Boolean = false,

        // Rating and reviews (placeholder for future implementation or if needed by UI)
        val isSubmittingRating: Boolean = false,
        val showBookingResultDialog: Boolean = false,
        val bookingResultMessage: String = "",
        val isBookingSuccess: Boolean = false,
        val hasConflict: Boolean = false,
        val bookerName: String = "",
        val bookerPhone: String = "",
        val showBookerErrors: Boolean = false,
        val showCancelDialog: Boolean = false,
        val cancelReason: String = "",
        val bookingToCancel: Long? = null,
        val showNotificationPermissionDialog: Boolean = false,
        val showPermanentlyDeniedDialog: Boolean = false,
        val triggerNotificationRequest: Boolean = false
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateBack : Effect
        data class NavigateToEdit(val stadium: StadiumResponse) : Effect
        data class ShowToast(val message: String) : Effect
        data class ShowBookingResult(val message: String, val isSuccess: Boolean) : Effect
    }

    sealed interface Event : MviEvent {
        object BackClick : Event
        object EditClick : Event
        data class ToggleActive(val isActive: Boolean) : Event
        /** Band qilingan slot bosildi - xodimga bron tafsilotini ko'rsatamiz. */
        data class SlotClick(val slot: SlotDto, val stadiumId: Int) : Event
        object DismissBookingDetails : Event
        object DismissBookingResultDialog : Event

        // New events for the improved logic
        data class SelectDate(val date: String) : Event
        data class SelectDuration(val durationKey: String) : Event
        data class SelectSlotSelection(val pitchIndex: Int, val startIndex: Int) : Event
        object ClearSelection : Event
        object Refresh : Event
        data class CreateBooking(
            val stadiumId: Int,
            val startTime: String,
            val endTime: String,
            val price: Double,
            val name: String? = null,
            val phone: String? = null
        ) : Event
        data class UpdateBookerName(val name: String) : Event
        data class UpdateBookerPhone(val phone: String) : Event
        data class OpenCancelDialog(val bookingId: Long) : Event
        object DismissCancelDialog : Event
        data class UpdateCancelReason(val reason: String) : Event
        data class ConfirmCancelBooking(val bookingId: Long, val reason: String) : Event
        data class SetShowNotificationPermissionDialog(val show: Boolean) : Event
        object RequestNotificationPermission : Event
        object DismissPermanentlyDeniedDialog : Event
        object OpenSettings : Event
        data class OnNotificationPermissionResult(val status: uz.coder.foottopbusiness.core.platform.PermissionStatus) : Event
    }
}
