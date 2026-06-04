package uz.coder.foottopbusiness.presentation.main.booking.list

import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.domain.usecase.booking.CancelBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsUseCase

class BookingListViewModel(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase
) : BaseViewModel<BookingListContract.State, BookingListContract.Effect, BookingListContract.Event>(
    initialState = BookingListContract.State()
) {
    init {
        loadBookings()
    }

    private fun loadBookings(isRefreshing: Boolean = false) {
        val s = state.value
        executeAsync(
            onLoading = { updateState { copy(isLoading = !isRefreshing, isRefreshing = isRefreshing) } },
            block = {
                getBookingsUseCase(
                    startDateFrom = s.startDate,
                    startDateTo = s.endDate,
                    stadiumId = s.stadiumId
                ).first()
            },
            onSuccess = { list ->
                updateState { 
                    copy(
                        bookings = list, 
                        isLoading = false, 
                        isRefreshing = false,
                        filteredBookings = filterBookings(list, selectedTab)
                    ) 
                }
            },
            onError = {
                updateState { copy(isLoading = false, isRefreshing = false) }
                sendEffect(BookingListContract.Effect.ShowToast("Xatolik: ${it.message}"))
            }
        )
    }

    override fun handleEvent(event: BookingListContract.Event) {
        when (event) {
            BookingListContract.Event.BackClick -> sendEffect(BookingListContract.Effect.NavigateBack)
            BookingListContract.Event.Refresh -> loadBookings(isRefreshing = true)
            is BookingListContract.Event.ChangeTab -> {
                updateState { 
                    copy(
                        selectedTab = event.index,
                        filteredBookings = filterBookings(bookings, event.index)
                    ) 
                }
            }
            is BookingListContract.Event.SelectBooking -> {
                sendEffect(BookingListContract.Effect.NavigateToDetails(event.booking))
            }
            is BookingListContract.Event.OpenCancelDialog -> {
                updateState { copy(showCancelDialog = true, bookingToCancel = event.bookingId, cancelReason = "") }
            }
            BookingListContract.Event.DismissCancelDialog -> {
                updateState { copy(showCancelDialog = false, bookingToCancel = null) }
            }
            is BookingListContract.Event.UpdateCancelReason -> {
                updateState { copy(cancelReason = event.reason) }
            }
            is BookingListContract.Event.ConfirmCancelBooking -> {
                cancelBooking(event.bookingId, event.reason)
            }
            is BookingListContract.Event.FilterByDate -> {
                updateState { copy(startDate = event.start, endDate = event.end) }
                loadBookings()
            }
        }
    }

    private fun filterBookings(list: List<BookingResponseDto>, tab: Int): List<BookingResponseDto> {
        val now = kotlin.time.Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        
        return when (tab) {
            1 -> { // Upcoming: CONFIRMED or PENDING and startTime > now
                list.filter { 
                    (it.status == "CONFIRMED" || it.status == "PENDING") && 
                    (it.startTime.toLocalDateTimeSafe()?.toInstant(tz)?.let { it > now } ?: false)
                }
            }
            2 -> { // Active: startTime <= now <= endTime
                list.filter {
                    val start = it.startTime.toLocalDateTimeSafe()?.toInstant(tz)
                    val end = it.endTime.toLocalDateTimeSafe()?.toInstant(tz)
                    start != null && end != null && start <= now && end >= now
                }
            }
            3 -> { // Completed: endTime < now and not CANCELLED
                list.filter {
                    it.status != "CANCELLED" && 
                    (it.endTime.toLocalDateTimeSafe()?.toInstant(tz)?.let { it < now } ?: false)
                }
            }
            4 -> { // Cancelled
                list.filter { it.status == "CANCELLED" }
            }
            else -> list // All
        }
    }

    private fun cancelBooking(id: Long, reason: String) {
        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            block = { cancelBookingUseCase(id, reason).first() },
            onSuccess = {
                updateState { copy(isLoading = false, showCancelDialog = false) }
                sendEffect(BookingListContract.Effect.ShowToast("Bron bekor qilindi"))
                loadBookings()
            },
            onError = {
                updateState { copy(isLoading = false) }
                sendEffect(BookingListContract.Effect.ShowToast("Xatolik: ${it.message}"))
            }
        )
    }
}
