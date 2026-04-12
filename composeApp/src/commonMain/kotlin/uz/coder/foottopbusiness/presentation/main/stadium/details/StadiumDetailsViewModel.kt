package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

class StadiumDetailsViewModel(
    stadium: StadiumResponse,
    private val stadiumRepository: StadiumRepository
) : BaseViewModel<StadiumDetailsContract.State, StadiumDetailsContract.Effect, StadiumDetailsContract.Event>(
    initialState = StadiumDetailsContract.State(stadium = stadium)
) {
    init {
        refreshStadium()
    }

    private fun refreshStadium() {
        val currentStadium = state.value.stadium ?: return
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = "${now.year}-${now.month.
        number.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
        
        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            block = {
                var result: List<StadiumResponse>? = null
                stadiumRepository.getStadiumById(
                    id = currentStadium.id ?: return@executeAsync emptyList(),
                    date = dateStr,
                    duration = currentStadium.duration ?: "01:00"
                ).collect { result = it }
                result
            },
            onSuccess = { list ->
                updateState { copy(stadium = list?.firstOrNull() ?: currentStadium, isLoading = false) }
            },
            onError = {
                updateState { copy(isLoading = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Ma'lumotlarni yangilashda xatolik: ${it.message}"))
            }
        )
    }
    override fun handleEvent(event: StadiumDetailsContract.Event) {
        when (event) {
            StadiumDetailsContract.Event.BackClick -> sendEffect(StadiumDetailsContract.Effect.NavigateBack)
            StadiumDetailsContract.Event.EditClick -> {
                state.value.stadium?.let {
                    sendEffect(StadiumDetailsContract.Effect.NavigateToEdit(it))
                }
            }
            is StadiumDetailsContract.Event.ToggleActive -> {
                updateStatus(event.isActive)
            }
            StadiumDetailsContract.Event.ShowAddPitchDialog -> {
                updateState { copy(showAddPitchDialog = true) }
            }
            StadiumDetailsContract.Event.DismissAddPitchDialog -> {
                updateState {
                    copy(
                        showAddPitchDialog = false,
                        pitchName = "",
                        pitchStartTime = "",
                        pitchEndTime = ""
                    )
                }
            }
            is StadiumDetailsContract.Event.PitchNameChanged -> {
                updateState { copy(pitchName = event.name) }
            }
            is StadiumDetailsContract.Event.PitchStartTimeChanged -> {
                updateState { copy(pitchStartTime = event.time) }
            }
            is StadiumDetailsContract.Event.PitchEndTimeChanged -> {
                updateState { copy(pitchEndTime = event.time) }
            }
            StadiumDetailsContract.Event.SavePitch -> {
                savePitch()
            }
            is StadiumDetailsContract.Event.SlotClick -> {
                updateState { copy(selectedSlot = event.slot, showSlotActionDialog = true) }
            }
            StadiumDetailsContract.Event.DismissSlotDialog -> {
                updateState { copy(showSlotActionDialog = false, selectedSlot = null) }
            }
            StadiumDetailsContract.Event.BookSlot -> {
                bookSlot()
            }
        }
    }

    private fun bookSlot() {
        val slot = state.value.selectedSlot ?: return
        // Trigger booking API
        sendEffect(StadiumDetailsContract.Effect.ShowToast("Band qilish so'rovi yuborildi: ${slot.start}"))
        updateState { copy(showSlotActionDialog = false, selectedSlot = null) }
    }

    private fun updateStatus(isActive: Boolean) {
        val currentStadium = state.value.stadium ?: return
        updateState { copy(isUpdatingStatus = true) }

        executeAsync(
            block = {
                var updated: StadiumResponse? = null
                stadiumRepository.updateStadium(
                    id = currentStadium.id ?: return@executeAsync null,
                    name = currentStadium.name ?: "",
                    description = currentStadium.description ?: "",
                    type = currentStadium.type ?: "",
                    duration = currentStadium.duration ?: "",
                    capacity = currentStadium.capacity ?: 0,
                    pricePerHour = currentStadium.pricePerHour?.toInt() ?: 0,
                    openTime = currentStadium.openTime ?: "",
                    closeTime = currentStadium.closeTime ?: "",
                    imageUrl = "",
                    regionId = 13,
                    districtId = 193,
                    isActive = isActive
                ).collect { updated = it }
                updated
            },
            onSuccess = { updatedStadium ->
                updateState { copy(stadium = updatedStadium ?: currentStadium, isUpdatingStatus = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Stadion holati yangilandi"))
            },
            onError = {
                updateState { copy(isUpdatingStatus = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Xatolik: ${it.message}"))
            }
        )
    }

    private fun savePitch() {
        val s = state.value
        if (s.pitchName.isBlank() || s.pitchStartTime.isBlank() || s.pitchEndTime.isBlank()) {
            sendEffect(StadiumDetailsContract.Effect.ShowToast("Barcha maydonlarni to'ldiring"))
            return
        }

        // Validation for HH:mm format
        val timeRegex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        if (!timeRegex.matches(s.pitchStartTime) || !timeRegex.matches(s.pitchEndTime)) {
            sendEffect(StadiumDetailsContract.Effect.ShowToast("Vaqt formati noto'g'ri (HH:mm)"))
            return
        }

        val pitch = PitchDto(
            name = s.pitchName,
            startTime = s.pitchStartTime,
            endTime = s.pitchEndTime
        )

        // TODO: API ga yuborish kerak
        log("Pitch", "Adding pitch: name=${pitch.name}, start=${pitch.startTime}, end=${pitch.endTime}")

        updateState {
            copy(
                pitches = pitches + pitch,
                showAddPitchDialog = false,
                pitchName = "",
                pitchStartTime = "",
                pitchEndTime = ""
            )
        }
        sendEffect(StadiumDetailsContract.Effect.ShowToast("Pitch muvaffaqiyatli qo'shildi"))
    }
}
