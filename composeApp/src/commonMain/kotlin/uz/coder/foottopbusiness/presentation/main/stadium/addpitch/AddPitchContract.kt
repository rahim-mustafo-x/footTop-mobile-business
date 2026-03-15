package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

enum class WeekDay(val label: String, val short: String) {
    Monday("Monday", "Mon"),
    Tuesday("Tuesday", "Tue"),
    Wednesday("Wednesday", "Wed"),
    Thursday("Thursday", "Thu"),
    Friday("Friday", "Fri"),
    Saturday("Saturday", "Sat"),
    Sunday("Sunday", "Sun")
}

data class TimeFrame(
    val id: Int,
    val startTime: String = "",
    val endTime: String = "",
    val price: String = ""
)

sealed interface AddPitchContract {
    data class State(
        val pitchName: String = "",
        val selectedDay: WeekDay = WeekDay.Monday,
        val schedules: Map<WeekDay, List<TimeFrame>> = WeekDay.values().associateWith { emptyList() },
        val showCopyDialog: Boolean = false,
        val copyTargetDays: Set<WeekDay> = emptySet(),
        val isLoading: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateBack : Effect
    }

    sealed interface Event : MviEvent {
        data class PitchName(val value: String) : Event
        data class SelectDay(val day: WeekDay) : Event
        data class AddTimeFrame(val day: WeekDay) : Event
        data class RemoveTimeFrame(val day: WeekDay, val frameId: Int) : Event
        data class UpdateStartTime(val day: WeekDay, val frameId: Int, val time: String) : Event
        data class UpdateEndTime(val day: WeekDay, val frameId: Int, val time: String) : Event
        data class UpdatePrice(val day: WeekDay, val frameId: Int, val price: String) : Event
        object ShowCopyDialog : Event
        object DismissCopyDialog : Event
        data class ToggleCopyDay(val day: WeekDay) : Event
        object ConfirmCopy : Event
        object Save : Event
    }
}
