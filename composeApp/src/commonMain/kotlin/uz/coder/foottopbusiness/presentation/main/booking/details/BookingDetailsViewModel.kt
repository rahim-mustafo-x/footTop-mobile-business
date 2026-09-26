package uz.coder.foottopbusiness.presentation.main.booking.details

import kotlinx.coroutines.flow.first
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.domain.usecase.booking.CancelSeriesUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.ConfirmBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.ConfirmSeriesUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.RejectBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.RejectSeriesUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.UpdatePaymentStatusUseCase

class BookingDetailsViewModel(
    booking: BookingResponseDto,
    private val confirmBookingUseCase: ConfirmBookingUseCase,
    private val rejectBookingUseCase: RejectBookingUseCase,
    private val updatePaymentStatusUseCase: UpdatePaymentStatusUseCase,
    private val confirmSeriesUseCase: ConfirmSeriesUseCase,
    private val rejectSeriesUseCase: RejectSeriesUseCase,
    private val cancelSeriesUseCase: CancelSeriesUseCase
) : BaseViewModel<BookingDetailsContract.State, BookingDetailsContract.Effect, BookingDetailsContract.Event>(
    initialState = BookingDetailsContract.State(booking = booking)
) {
    override fun handleEvent(event: BookingDetailsContract.Event) {
        when (event) {
            BookingDetailsContract.Event.Confirm -> confirm()
            BookingDetailsContract.Event.OpenRejectDialog -> {
                updateState { copy(showRejectDialog = true, rejectReason = "", rejectWholeSeries = false) }
            }
            BookingDetailsContract.Event.OpenRejectSeriesDialog -> {
                updateState { copy(showRejectDialog = true, rejectReason = "", rejectWholeSeries = true) }
            }
            BookingDetailsContract.Event.DismissRejectDialog -> {
                updateState { copy(showRejectDialog = false) }
            }
            is BookingDetailsContract.Event.UpdateRejectReason -> {
                updateState { copy(rejectReason = event.reason) }
            }
            BookingDetailsContract.Event.SubmitReject -> {
                if (state.value.rejectWholeSeries) rejectSeries() else reject()
            }
            BookingDetailsContract.Event.TogglePaymentStatus -> togglePaymentStatus()
            BookingDetailsContract.Event.ConfirmSeries -> confirmSeries()
            BookingDetailsContract.Event.OpenCancelSeriesDialog -> {
                updateState { copy(showCancelSeriesDialog = true, cancelSeriesReason = "") }
            }
            BookingDetailsContract.Event.DismissCancelSeriesDialog -> {
                updateState { copy(showCancelSeriesDialog = false) }
            }
            is BookingDetailsContract.Event.UpdateCancelSeriesReason -> {
                updateState { copy(cancelSeriesReason = event.reason) }
            }
            BookingDetailsContract.Event.SubmitCancelSeries -> cancelSeries()
        }
    }

    private fun confirm() {
        val id = state.value.booking.id ?: return
        if (state.value.isProcessing) return
        executeAsync(
            onLoading = { updateState { copy(isProcessing = true) } },
            block = { confirmBookingUseCase(id).first() },
            onSuccess = { updated ->
                updateState { copy(booking = updated, isProcessing = false) }
                sendEffect(BookingDetailsContract.Effect.BookingConfirmed)
            },
            onError = ::onError
        )
    }

    private fun reject() {
        val s = state.value
        val id = s.booking.id ?: return
        val reason = s.rejectReason.trim()
        if (s.isProcessing || reason.isEmpty()) return
        executeAsync(
            onLoading = { updateState { copy(isProcessing = true) } },
            block = { rejectBookingUseCase(id, reason).first() },
            onSuccess = { updated ->
                updateState { copy(booking = updated, isProcessing = false, showRejectDialog = false) }
                sendEffect(BookingDetailsContract.Effect.BookingRejected)
            },
            onError = ::onError
        )
    }

    private fun togglePaymentStatus() {
        val s = state.value
        val id = s.booking.id ?: return
        if (s.isProcessing) return
        val target = if (s.booking.paymentStatus == PAID) UNPAID else PAID
        executeAsync(
            onLoading = { updateState { copy(isProcessing = true) } },
            block = { updatePaymentStatusUseCase(id, target).first() },
            onSuccess = { updated ->
                updateState { copy(booking = updated, isProcessing = false) }
                sendEffect(BookingDetailsContract.Effect.PaymentStatusUpdated)
            },
            onError = ::onError
        )
    }

    private fun confirmSeries() {
        val groupId = state.value.booking.recurrenceGroupId ?: return
        if (state.value.isProcessing) return
        executeAsync(
            onLoading = { updateState { copy(isProcessing = true) } },
            block = { confirmSeriesUseCase(groupId).first() },
            onSuccess = { series ->
                applySeries(series)
                sendEffect(BookingDetailsContract.Effect.SeriesUpdated)
            },
            onError = ::onError
        )
    }

    private fun rejectSeries() {
        val s = state.value
        val groupId = s.booking.recurrenceGroupId ?: return
        val reason = s.rejectReason.trim()
        if (s.isProcessing || reason.isEmpty()) return
        executeAsync(
            onLoading = { updateState { copy(isProcessing = true) } },
            block = { rejectSeriesUseCase(groupId, reason).first() },
            onSuccess = { series ->
                applySeries(series)
                updateState { copy(showRejectDialog = false) }
                sendEffect(BookingDetailsContract.Effect.SeriesUpdated)
            },
            onError = ::onError
        )
    }

    private fun cancelSeries() {
        val s = state.value
        val groupId = s.booking.recurrenceGroupId ?: return
        val reason = s.cancelSeriesReason.trim()
        if (s.isProcessing || reason.isEmpty()) return
        executeAsync(
            onLoading = { updateState { copy(isProcessing = true) } },
            block = { cancelSeriesUseCase(groupId, reason).first() },
            onSuccess = { series ->
                applySeries(series)
                updateState { copy(showCancelSeriesDialog = false) }
                sendEffect(BookingDetailsContract.Effect.SeriesUpdated)
            },
            onError = ::onError
        )
    }

    /** Seriya javobidan ochiq turgan bronning yangi holatini olamiz. */
    private fun applySeries(series: List<BookingResponseDto>) {
        updateState {
            copy(
                booking = series.firstOrNull { it.id == booking.id } ?: booking,
                isProcessing = false
            )
        }
    }

    private fun onError(error: Throwable) {
        updateState { copy(isProcessing = false) }
        sendEffect(BookingDetailsContract.Effect.ShowError(error.message.orEmpty()))
    }

    private companion object {
        const val PAID = "PAID"
        const val UNPAID = "UNPAID"
    }
}
