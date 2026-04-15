package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.stadium.SlotDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

// TODO: Pitch - name, start_time, end_time
data class PitchDto(
    val name: String,
    val startTime: String,
    val endTime: String
)

sealed interface StadiumDetailsContract {
    data class State(
        val stadium: StadiumResponse? = null,
        val isLoading: Boolean = false,
        val isUpdatingStatus: Boolean = false,
        val showAddPitchDialog: Boolean = false,
        val pitchName: String = "",
        val pitchStartTime: String = "",
        val pitchEndTime: String = "",
        val pitches: List<PitchDto> = emptyList(),
        val selectedSlot: SlotDto? = null,
        val showSlotActionDialog: Boolean = false
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateBack : Effect
        data class NavigateToEdit(val stadium: StadiumResponse) : Effect
        data class ShowToast(val message: String) : Effect
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
    }
}
