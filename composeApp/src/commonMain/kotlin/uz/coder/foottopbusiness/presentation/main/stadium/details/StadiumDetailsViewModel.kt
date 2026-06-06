package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.data.network.dto.stadium.SlotDto
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import uz.coder.foottopbusiness.core.platform.checkNotificationPermissionStatus
import uz.coder.foottopbusiness.core.platform.requestNotificationPermission
import uz.coder.foottopbusiness.core.platform.PermissionStatus
import uz.coder.foottopbusiness.core.platform.openAppSettings
import kotlinx.datetime.*
import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.domain.usecase.booking.CreateBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.CancelBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsByStadiumIdUseCase
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

fun slotsNeededForDuration(mins: Int): Int = (mins / 30) + 1

class StadiumDetailsViewModel(
    stadium: StadiumResponse,
    private val stadiumRepository: StadiumRepository,
    private val preferencesManager: PreferencesManager,
    private val createBookingUseCase: CreateBookingUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val getBookingsByStadiumIdUseCase: GetBookingsByStadiumIdUseCase
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
                val s = state.value
                val isStaff = s.userRole == UserRole.SUPER_ADMIN || s.userRole == UserRole.DISTRICT_ADMIN || s.userRole == UserRole.OWNER
                if (event.slot.status == "BOOKED" && isStaff) {
                    fetchBookingDetails(event.slot)
                } else {
                    updateState { copy(selectedSlot = event.slot, showSlotActionDialog = true) }
                }
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
                if (state.value.bookerName.isBlank() || state.value.bookerPhone.length < 9) {
                    updateState { copy(showBookerErrors = true) }
                    sendEffect(StadiumDetailsContract.Effect.ShowToast("Iltimos, ma'lumotlarni to'liq kiriting"))
                    return
                }
                bookSelectedSlots(event)
            }
            StadiumDetailsContract.Event.DismissBookingResultDialog -> {
                updateState { copy(showBookingResultDialog = false) }
            }
            is StadiumDetailsContract.Event.UpdateBookerName -> {
                updateState { copy(bookerName = event.name, showBookerErrors = false) }
            }
            is StadiumDetailsContract.Event.UpdateBookerPhone -> {
                updateState { copy(bookerPhone = event.phone, showBookerErrors = false) }
            }
            is StadiumDetailsContract.Event.OpenCancelDialog -> {
                updateState { copy(showCancelDialog = true, bookingToCancel = event.bookingId, cancelReason = "") }
            }
            StadiumDetailsContract.Event.DismissCancelDialog -> {
                updateState { copy(showCancelDialog = false, bookingToCancel = null) }
            }
            is StadiumDetailsContract.Event.UpdateCancelReason -> {
                updateState { copy(cancelReason = event.reason) }
            }
            is StadiumDetailsContract.Event.ConfirmCancelBooking -> {
                cancelBooking(event.bookingId, event.reason)
            }
            is StadiumDetailsContract.Event.SetShowNotificationPermissionDialog -> {
                updateState { copy(showNotificationPermissionDialog = event.show) }
            }
            StadiumDetailsContract.Event.RequestNotificationPermission -> {
                updateState { copy(showNotificationPermissionDialog = false, triggerNotificationRequest = true) }
            }
            is StadiumDetailsContract.Event.OnNotificationPermissionResult -> {
                updateState { copy(triggerNotificationRequest = false) }
                handlePermissionResult(event.status)
            }
            StadiumDetailsContract.Event.DismissPermanentlyDeniedDialog -> {
                updateState { copy(showPermanentlyDeniedDialog = false) }
            }
            StadiumDetailsContract.Event.OpenSettings -> {
                updateState { copy(showPermanentlyDeniedDialog = false) }
                openAppSettings()
            }
        }
    }

    private fun handlePermissionResult(status: PermissionStatus) {
        viewModelScope.launch {
            val currentCount = preferencesManager.notificationRequestCount.first()
            preferencesManager.setNotificationRequestCount(currentCount + 1)

            if (status == PermissionStatus.GRANTED) {
                preferencesManager.setNotificationPermission(true)
            } else {
                if (currentCount >= 2) {
                    updateState { copy(showPermanentlyDeniedDialog = true) }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        executeAsync {
            val grantedInPrefs = preferencesManager.notificationPermission.first()
            if (!grantedInPrefs) {
                val status = checkNotificationPermissionStatus()
                if (status != PermissionStatus.GRANTED) {
                    val requestCount = preferencesManager.notificationRequestCount.first()
                    if (requestCount <= 2) {
                        updateState { copy(showNotificationPermissionDialog = true) }
                    } else {
                        updateState { copy(showPermanentlyDeniedDialog = true) }
                    }
                } else {
                    preferencesManager.setNotificationPermission(true)
                }
            }
        }
    }

    private fun fetchBookingDetails(slot: SlotDto) {
        // Since we don't have a direct "get booking by slot id" we might need to fetch all bookings for this stadium and date
        val s = state.value
        val stadiumId = s.stadium?.id?.toLong() ?: return
        val date = s.selectedDate ?: return

        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            block = {
                getBookingsByStadiumIdUseCase(stadiumId, date).first()
            },
            onSuccess = { bookings ->
                updateState { copy(isLoading = false) }
                // Match the booking by start time since SlotDto doesn't have an ID
                val booking = bookings.firstOrNull { it.startTime == slot.start }
                if (booking != null) {
                     sendEffect(StadiumDetailsContract.Effect.ShowToast("Band qilingan: ${booking.name ?: "Noma'lum"} (${booking.phone ?: "-"})"))
                } else {
                     sendEffect(StadiumDetailsContract.Effect.ShowToast("Bron ma'lumotlari topilmadi"))
                }
            },
            onError = {
                updateState { copy(isLoading = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Bron ma'lumotlarini yuklashda xatolik"))
            }
        )
    }

    private fun cancelBooking(id: Long, reason: String) {
        if (reason.isBlank()) {
            sendEffect(StadiumDetailsContract.Effect.ShowToast("Sababini kiriting"))
            return
        }
        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            block = { cancelBookingUseCase(id, reason).first() },
            onSuccess = {
                updateState { copy(isLoading = false, showCancelDialog = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Bron bekor qilindi"))
                refreshStadium()
            },
            onError = {
                updateState { copy(isLoading = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Xatolik: ${it.message}"))
            }
        )
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
                    paymentMethod = "CASH",
                    name = event.name,
                    phone = event.phone
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
                        selectedStartIndex = null,
                        bookerName = "",
                        bookerPhone = ""
                    ) 
                }
                checkNotificationPermission()
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
