package uz.coder.foottopbusiness.presentation.main.booking.list

import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.domain.repository.BookingRepository
import uz.coder.foottopbusiness.domain.usecase.booking.CancelBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.ConfirmBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.RejectBookingUseCase

class BookingListViewModel(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val confirmBookingUseCase: ConfirmBookingUseCase,
    private val rejectBookingUseCase: RejectBookingUseCase
) : BaseViewModel<BookingListContract.State, BookingListContract.Effect, BookingListContract.Event>(
    initialState = BookingListContract.State()
) {
    init {
        loadBookings()
    }

    /**
     * Backend endi sahifalab qaytaradi. [append] = true bo'lsa keyingi sahifa
     * mavjud ro'yxatga qo'shiladi, aks holda ro'yxat noldan yuklanadi.
     */
    private fun loadBookings(isRefreshing: Boolean = false, append: Boolean = false) {
        val s = state.value
        val targetPage = if (append) s.page + 1 else 0
        executeAsync(
            onLoading = {
                updateState {
                    copy(
                        isLoading = !isRefreshing && !append,
                        isRefreshing = isRefreshing,
                        isLoadingMore = append
                    )
                }
            },
            block = {
                getBookingsUseCase(
                    startDateFrom = s.startDate,
                    startDateTo = s.endDate,
                    stadiumId = s.stadiumId,
                    page = targetPage
                ).first()
            },
            onSuccess = { list ->
                updateState {
                    val merged = if (append) bookings + list else list
                    copy(
                        bookings = merged,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        page = targetPage,
                        canLoadMore = list.size >= BookingRepository.DEFAULT_PAGE_SIZE,
                        filteredBookings = filterBookings(merged, selectedTab)
                    )
                }
            },
            onError = {
                updateState { copy(isLoading = false, isRefreshing = false, isLoadingMore = false) }
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
            is BookingListContract.Event.ConfirmBooking -> confirmBooking(event.bookingId)
            is BookingListContract.Event.OpenRejectDialog -> {
                updateState { copy(showRejectDialog = true, bookingToReject = event.bookingId, rejectReason = "") }
            }
            BookingListContract.Event.DismissRejectDialog -> {
                updateState { copy(showRejectDialog = false, bookingToReject = null) }
            }
            is BookingListContract.Event.UpdateRejectReason -> {
                updateState { copy(rejectReason = event.reason) }
            }
            is BookingListContract.Event.SubmitReject -> rejectBooking(event.bookingId, event.reason)
            BookingListContract.Event.LoadMore -> {
                val s = state.value
                if (s.canLoadMore && !s.isLoadingMore && !s.isLoading) {
                    loadBookings(append = true)
                }
            }
        }
    }

    private fun confirmBooking(id: Long) {
        executeAsync(
            onLoading = { updateState { copy(processingBookingId = id) } },
            block = { confirmBookingUseCase(id).first() },
            onSuccess = { updated ->
                updateState { copy(processingBookingId = null) }
                replaceBooking(updated)
                sendEffect(BookingListContract.Effect.BookingConfirmed)
            },
            onError = {
                updateState { copy(processingBookingId = null) }
                sendEffect(BookingListContract.Effect.ShowToast("Xatolik: ${it.message}"))
            }
        )
    }

    private fun rejectBooking(id: Long, reason: String) {
        executeAsync(
            onLoading = { updateState { copy(processingBookingId = id) } },
            block = { rejectBookingUseCase(id, reason).first() },
            onSuccess = { updated ->
                updateState { copy(processingBookingId = null, showRejectDialog = false, bookingToReject = null) }
                replaceBooking(updated)
                sendEffect(BookingListContract.Effect.BookingRejected)
            },
            onError = {
                updateState { copy(processingBookingId = null) }
                sendEffect(BookingListContract.Effect.ShowToast("Xatolik: ${it.message}"))
            }
        )
    }

    /** Butun ro'yxatni qayta yuklamasdan, faqat o'zgargan bronni almashtiramiz. */
    private fun replaceBooking(updated: BookingResponseDto) {
        updateState {
            val merged = bookings.map { if (it.id == updated.id) updated else it }
            copy(bookings = merged, filteredBookings = filterBookings(merged, selectedTab))
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
                list.filter { it.status == "CANCELLED" || it.status == "REJECTED" }
            }
            5 -> { // Tasdiqlashni kutayotganlar -- egasining asosiy ish ro'yxati
                list.filter { it.status == "PENDING" }
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
