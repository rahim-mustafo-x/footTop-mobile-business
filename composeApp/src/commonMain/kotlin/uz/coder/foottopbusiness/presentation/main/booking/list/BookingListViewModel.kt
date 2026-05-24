package uz.coder.foottopbusiness.presentation.main.booking.list

import kotlinx.coroutines.flow.first
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
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

    private fun loadBookings() {
        val s = state.value
        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            block = {
                getBookingsUseCase(
                    startDateFrom = s.startDate,
                    startDateTo = s.endDate,
                    stadiumId = s.stadiumId
                ).first()
            },
            onSuccess = { list ->
                updateState { copy(bookings = list, isLoading = false) }
            },
            onError = {
                updateState { copy(isLoading = false) }
                sendEffect(BookingListContract.Effect.ShowToast("Xatolik: ${it.message}"))
            }
        )
    }

    override fun handleEvent(event: BookingListContract.Event) {
        when (event) {
            BookingListContract.Event.BackClick -> sendEffect(BookingListContract.Effect.NavigateBack)
            BookingListContract.Event.Refresh -> loadBookings()
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
