package uz.coder.foottopbusiness.presentation.main.reports

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.domain.repository.BookingRepository
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.UpdatePaymentStatusUseCase
import uz.coder.foottopbusiness.domain.usecase.owner.GetOwnerReportUseCase
import kotlin.time.Clock

/** CSV eksportda bronlarni sahifalab olishda xavfsizlik chegarasi. */
private const val MAX_PAGES = 40

/** Bronlar asosidagi hisobot: tushum, qarzlar, bandlik va gavjum soatlar (owner-report). */
class ReportsViewModel(
    private val getOwnerReportUseCase: GetOwnerReportUseCase,
    private val updatePaymentStatusUseCase: UpdatePaymentStatusUseCase,
    private val getBookingsUseCase: GetBookingsUseCase,
) : BaseViewModel<ReportsContract.State, ReportsContract.Effect, ReportsContract.Event>(
    initialState = ReportsContract.State()
) {
    private var loadJob: Job? = null

    init {
        load()
    }

    override fun handleEvent(event: ReportsContract.Event) {
        when (event) {
            ReportsContract.Event.Load -> load()
            is ReportsContract.Event.SelectPeriod -> {
                updateState { copy(period = event.period) }
                load()
            }
            is ReportsContract.Event.MarkPaid -> markPaid(event.bookingId)
            ReportsContract.Event.DownloadReport -> exportCsv()
        }
    }

    private fun range(period: ReportPeriod): Pair<LocalDate, LocalDate> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val from = when (period) {
            ReportPeriod.WEEK -> today.minus(6, DateTimeUnit.DAY)
            ReportPeriod.DAYS_30 -> today.minus(29, DateTimeUnit.DAY)
            ReportPeriod.MONTH -> LocalDate(today.year, today.month, 1)
        }
        return from to today
    }

    private fun load() {
        loadJob?.cancel()
        val (from, to) = range(state.value.period)
        updateState { copy(from = from.toString(), to = to.toString()) }
        loadJob = executeAsync(
            onLoading = { updateState { copy(isLoading = true, error = null) } },
            block = { getOwnerReportUseCase(from.toString(), to.toString()).first() },
            onSuccess = { report -> updateState { copy(isLoading = false, report = report) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message.orEmpty()) } }
        )
    }

    private fun markPaid(id: Long) {
        if (state.value.payingId != null) return
        executeAsync(
            onLoading = { updateState { copy(payingId = id) } },
            block = { updatePaymentStatusUseCase(id, "PAID").first() },
            onSuccess = {
                updateState { copy(payingId = null) }
                sendEffect(ReportsContract.Effect.PaymentUpdated)
                load()
            },
            onError = { e ->
                updateState { copy(payingId = null) }
                sendEffect(ReportsContract.Effect.ShowError(e.message.orEmpty()))
            }
        )
    }

    /** Davrdagi bronlar CSV (Excel'da ochiladi). */
    private fun exportCsv() {
        val s = state.value
        if (s.isExporting || s.from.isEmpty()) return
        executeAsync(
            onLoading = { updateState { copy(isExporting = true) } },
            block = {
                val all = mutableListOf<BookingResponseDto>()
                for (page in 0 until MAX_PAGES) {
                    val chunk = getBookingsUseCase(startDateFrom = s.from, startDateTo = s.to, page = page).first()
                    all += chunk
                    if (chunk.size < BookingRepository.DEFAULT_PAGE_SIZE) break
                }
                toCsv(all)
            },
            onSuccess = { csv ->
                updateState { copy(isExporting = false) }
                sendEffect(ReportsContract.Effect.DownloadFile("bronlar_${s.from}_${s.to}.csv", csv))
            },
            onError = { e ->
                updateState { copy(isExporting = false) }
                sendEffect(ReportsContract.Effect.ShowError(e.message.orEmpty()))
            }
        )
    }

    private fun toCsv(bookings: List<BookingResponseDto>): String {
        val cell = { v: Any? ->
            val text = v?.toString().orEmpty()
            if (text.any { it == '"' || it == ',' || it == '\n' }) "\"${text.replace("\"", "\"\"")}\"" else text
        }
        val lines = mutableListOf("ID,Sana,Vaqt,Stadion,Mijoz,Telefon,Holat,Kelmadi,Narx,To'lov")
        bookings.forEach { b ->
            val date = b.startTime?.substringBefore('T')?.split('-')?.reversed()?.joinToString(".")
            lines += listOf(
                b.id,
                date,
                "${b.startTime.formatToTime()}-${b.endTime.formatToTime()}",
                b.stadiumName,
                b.name,
                b.phone,
                b.status,
                if (b.noShow == true) "ha" else "",
                (b.totalPrice ?: 0.0).toLong(),
                b.paymentStatus
            ).joinToString(",") { cell(it) }
        }
        // BOM: Excel UTF-8 ni to'g'ri o'qishi uchun
        return "﻿" + lines.joinToString("\n")
    }
}
