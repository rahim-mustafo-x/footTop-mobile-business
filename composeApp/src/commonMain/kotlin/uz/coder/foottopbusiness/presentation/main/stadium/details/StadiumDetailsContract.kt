package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
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
        val selectedSlot: SlotDto? = null,
        val showSlotActionDialog: Boolean = false,

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
        val hasConflict: Boolean = false
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
        data class SlotClick(val slot: SlotDto) : Event
        object DismissSlotDialog : Event
        data class BookSlot(val slot: SlotDto) : Event
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
            val price: Double
        ) : Event
    }
}
