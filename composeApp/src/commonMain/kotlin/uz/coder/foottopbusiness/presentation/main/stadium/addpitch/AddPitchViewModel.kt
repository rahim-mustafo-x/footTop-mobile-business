package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import uz.coder.foottopbusiness.core.mvi.BaseViewModel

class AddPitchViewModel : BaseViewModel<AddPitchContract.State, AddPitchContract.Effect, AddPitchContract.Event>(
    initialState = AddPitchContract.State()
) {
    private var nextId = 0

    override fun handleEvent(event: AddPitchContract.Event) {
        when (event) {
            is AddPitchContract.Event.PitchName -> updateState { copy(pitchName = event.value) }
            is AddPitchContract.Event.SelectDay -> updateState { copy(selectedDay = event.day) }

            is AddPitchContract.Event.AddTimeFrame -> updateState {
                val current = schedules[event.day] ?: emptyList()
                copy(schedules = schedules + (event.day to current + TimeFrame(id = nextId++)))
            }

            is AddPitchContract.Event.RemoveTimeFrame -> updateState {
                val current = schedules[event.day] ?: emptyList()
                copy(schedules = schedules + (event.day to current.filter { it.id != event.frameId }))
            }

            is AddPitchContract.Event.UpdateStartTime -> updateState {
                updateFrame(event.day, event.frameId) { copy(startTime = event.time) }
            }

            is AddPitchContract.Event.UpdateEndTime -> updateState {
                updateFrame(event.day, event.frameId) { copy(endTime = event.time) }
            }

            is AddPitchContract.Event.UpdatePrice -> updateState {
                updateFrame(event.day, event.frameId) { copy(price = event.price) }
            }

            is AddPitchContract.Event.ShowCopyDialog -> updateState {
                copy(showCopyDialog = true, copyTargetDays = emptySet())
            }

            is AddPitchContract.Event.DismissCopyDialog -> updateState {
                copy(showCopyDialog = false, copyTargetDays = emptySet())
            }

            is AddPitchContract.Event.ToggleCopyDay -> updateState {
                val updated = if (event.day in copyTargetDays)
                    copyTargetDays - event.day
                else
                    copyTargetDays + event.day
                copy(copyTargetDays = updated)
            }

            is AddPitchContract.Event.ConfirmCopy -> updateState {
                val sourceFrames = schedules[selectedDay] ?: emptyList()
                var newSchedules = schedules
                copyTargetDays.forEach { targetDay ->
                    val copied = sourceFrames.map { it.copy(id = nextId++) }
                    newSchedules = newSchedules + (targetDay to copied)
                }
                copy(schedules = newSchedules, showCopyDialog = false, copyTargetDays = emptySet())
            }

            is AddPitchContract.Event.Save -> sendEffect(AddPitchContract.Effect.NavigateBack)
        }
    }

    private fun AddPitchContract.State.updateFrame(
        day: WeekDay,
        frameId: Int,
        update: TimeFrame.() -> TimeFrame
    ): AddPitchContract.State {
        val current = schedules[day] ?: emptyList()
        val updated = current.map { if (it.id == frameId) it.update() else it }
        return copy(schedules = schedules + (day to updated))
    }
}
