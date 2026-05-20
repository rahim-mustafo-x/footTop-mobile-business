package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import kotlinx.datetime.*
import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.domain.usecase.booking.CreateBookingUseCase
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import kotlin.time.Duration.Companion.minutes

fun slotsNeededForDuration(mins: Int): Int = (mins / 30) + 1

class StadiumDetailsViewModel(
    stadium: StadiumResponse,
    private val stadiumRepository: StadiumRepository,
    private val preferencesManager: PreferencesManager,
    private val createBookingUseCase: CreateBookingUseCase
) : BaseViewModel<StadiumDetailsContract.State, StadiumDetailsContract.Effect, StadiumDetailsContract.Event>(
    initialState = StadiumDetailsContract.State(stadium = stadium)
) {
    private var bookingJob: Job? = null

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
        val dateStr = state.value.selectedDate ?: "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
        val duration = state.value.selectedDurationKey
        
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
                val s = state.value
                val st = s.stadiums.getOrNull(event.pitchIndex)
                val slots = st?.slots ?: emptyList()
                val clickedSlot = slots.getOrNull(event.startIndex)
                
                if (clickedSlot != null) {
                    val durationMins = StadiumDetailsContract.durationMinutesKey(s.selectedDurationKey)
                    val tz = TimeZone.currentSystemDefault()
                    val startDT = clickedSlot.start.toLocalDateTimeSafe()
                    val selectedEnd = startDT?.toInstant(tz)?.plus(durationMins.minutes)?.toLocalDateTime(tz)

                    // Conflict Check
                    val hasConflict = slots.any { slot ->
                        val slotStart = slot.start?.toLocalDateTimeSafe()
                        val slotEnd = slot.end?.toLocalDateTimeSafe()
                        slot.status != "AVAILABLE" &&
                                startDT != null && selectedEnd != null &&
                                slotStart != null && slotEnd != null &&
                                startDT < slotEnd && selectedEnd > slotStart
                    }

                    updateState { 
                        copy(
                            selectedPitchIndex = event.pitchIndex, 
                            selectedStartIndex = event.startIndex,
                            hasConflict = hasConflict
                        ) 
                    }
                }
            }
            StadiumDetailsContract.Event.ClearSelection -> {
                updateState { copy(selectedPitchIndex = null, selectedStartIndex = null, hasConflict = false) }
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
        if (bookingJob?.isActive == true) return
        
        updateState { copy(isBooking = true) }
        bookingJob = executeAsync(
            block = {
                val userId = preferencesManager.userId.first().toLong()
                val request = BookingRequestDto(
                    userId = userId,
                    stadiumId = event.stadiumId.toLong(),
                    startTime = event.startTime,
                    endTime = event.endTime,
                    totalPrice = event.price,
                    status = "PENDING",
                    paymentMethod = "CASH"
                )
                createBookingUseCase(request).first()
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
        val slot = state.value.selectedSlot ?: return
        updateState { copy(isBooking = true) }
        executeAsync(
            block = {
                val userId = preferencesManager.userId.first().toLong()
                val request = BookingRequestDto(
                    userId = userId,
                    stadiumId = state.value.stadium?.id?.toLong(),
                    startTime = slot.start,
                    endTime = slot.end,
                    totalPrice = state.value.stadium?.pricePerHour,
                    status = "PENDING",
                    paymentMethod = "CASH"
                )
                createBookingUseCase(request).first()
            },
            onSuccess = {
                updateState { copy(isBooking = false, showSlotActionDialog = false, selectedSlot = null) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Vaqt muvaffaqiyatli band qilindi"))
                refreshStadium()
            },
            onError = {
                updateState { copy(isBooking = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Band qilishda xatolik yuz berdi: ${it.message}"))
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
}
