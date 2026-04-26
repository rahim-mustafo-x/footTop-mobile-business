package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.stadium.SlotDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.model.UserRole

// TODO: Pitch - name, start_time, end_time
data class PitchDto(
    val name: String,
    val startTime: String,
    val endTime: String
)

sealed interface StadiumDetailsContract {
    data class State(
        val stadium: StadiumResponse? = null,
        val stadiums: List<StadiumResponse> = emptyList(), // Support for multiple pitches
        val userRole: UserRole = UserRole.UNKNOWN,
        val isLoading: Boolean = false,
        val isUpdatingStatus: Boolean = false,
        val isBooking: Boolean = false,
        val showAddPitchDialog: Boolean = false,
        val pitchName: String = "",
        val pitchStartTime: String = "",
        val pitchEndTime: String = "",
        val pitches: List<PitchDto> = emptyList(),
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
        val isBookingSuccess: Boolean = false
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
        object ShowAddPitchDialog : Event
        object DismissAddPitchDialog : Event
        data class PitchNameChanged(val name: String) : Event
        data class PitchStartTimeChanged(val time: String) : Event
        data class PitchEndTimeChanged(val time: String) : Event
        object SavePitch : Event
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
        
        data class SubmitRating(val rating: Int, val comment: String) : Event
    }
}
