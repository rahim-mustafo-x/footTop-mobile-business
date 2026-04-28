package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.model.UserRole
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant

fun durationMinutesKey(key: String): Int = when(key) {
    "SIXTY" -> 60
    "NINETY" -> 90
    "ONE_HUNDRED_TWENTY" -> 120
    else -> 60
}

fun slotsNeededForDuration(mins: Int): Int = mins / 30

class StadiumDetailsViewModel(
    stadium: StadiumResponse,
    private val stadiumRepository: StadiumRepository,
    private val preferencesManager: PreferencesManager
) : BaseViewModel<StadiumDetailsContract.State, StadiumDetailsContract.Effect, StadiumDetailsContract.Event>(
    initialState = StadiumDetailsContract.State(stadium = stadium)
) {
    init {
        loadUserRole()
        refreshStadium()
    }

    private fun loadUserRole() {
        executeAsync(
            block = {
                val roleStr = preferencesManager.role.firstOrNull()
                UserRole.fromString(roleStr)
            },
            onSuccess = { role ->
                updateState { copy(userRole = role) }
            }
        )
    }

    private fun refreshStadium() {
        val currentStadium = state.value.stadium ?: return
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = state.value.selectedDate ?: "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
        val duration = when(state.value.selectedDurationKey) {
            "SIXTY" -> "01:00"
            "NINETY" -> "01:30"
            "ONE_HUNDRED_TWENTY" -> "02:00"
            else -> "01:00"
        }
        
        executeAsync(
            onLoading = { updateState { copy(isLoading = true, isSlotsLoading = true) } },
            block = {
                var result: List<StadiumResponse>? = null
                stadiumRepository.getStadiumById(
                    id = currentStadium.id ?: return@executeAsync emptyList(),
                    date = dateStr,
                    duration = duration
                ).collect { result = it }
                result
            },
            onSuccess = { list ->
                updateState { 
                    copy(
                        stadium = list?.firstOrNull() ?: currentStadium,
                        stadiums = list ?: emptyList(),
                        isLoading = false,
                        isSlotsLoading = false,
                        selectedDate = dateStr
                    ) 
                }
            },
            onError = {
                updateState { copy(isLoading = false, isSlotsLoading = false) }
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
            is StadiumDetailsContract.Event.BookSlot -> {
                bookSlot()
            }
            is StadiumDetailsContract.Event.SelectDate -> {
                updateState { copy(selectedDate = event.date) }
                refreshStadium()
            }
            is StadiumDetailsContract.Event.SelectDuration -> {
                updateState { copy(selectedDurationKey = event.durationKey) }
                refreshStadium()
            }
            is StadiumDetailsContract.Event.SelectSlotSelection -> {
                updateState { copy(selectedPitchIndex = event.pitchIndex, selectedStartIndex = event.startIndex) }
            }
            StadiumDetailsContract.Event.ClearSelection -> {
                updateState { copy(selectedPitchIndex = null, selectedStartIndex = null) }
            }
            StadiumDetailsContract.Event.Refresh -> {
                refreshStadium()
            }
            is StadiumDetailsContract.Event.CreateBooking -> {
                bookSelectedSlots(event)
            }
            StadiumDetailsContract.Event.DismissBookingResultDialog -> {
                updateState { copy(showBookingResultDialog = false) }
            }
        }
    }

    private fun bookSelectedSlots(event: StadiumDetailsContract.Event.CreateBooking) {
        updateState { copy(isBooking = true) }
        executeAsync(
            block = {
                // In a real app, this would call a repository to create a booking
                kotlinx.coroutines.delay(1000)
                true
            },
            onSuccess = {
                updateState { 
                    copy(
                        isBooking = false, 
                        showBookingResultDialog = true, 
                        bookingResultMessage = "Stadion muvaffaqiyatli band qilindi!",
                        isBookingSuccess = true,
                        selectedPitchIndex = null,
                        selectedStartIndex = null
                    ) 
                }
                refreshStadium()
            },
            onError = {
                updateState { 
                    copy(
                        isBooking = false, 
                        showBookingResultDialog = true, 
                        bookingResultMessage = "Band qilishda xatolik: ${it.message}",
                        isBookingSuccess = false
                    ) 
                }
            }
        )
    }

    private fun bookSlot() {
        updateState { copy(isBooking = true) }
        executeAsync(
            block = {
                // In a real app, this would call a repository to create a booking or match
                // For now, we simulate success after a delay
                kotlinx.coroutines.delay(1000)
                true
            },
            onSuccess = {
                updateState { copy(isBooking = false, showSlotActionDialog = false, selectedSlot = null) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Vaqt muvaffaqiyatli band qilindi"))
                refreshStadium()
            },
            onError = {
                updateState { copy(isBooking = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Band qilishda xatolik yuz berdi"))
            }
        )
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
