package uz.coder.foottopbusiness.presentation.main.schedule

import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockRequestDto
import uz.coder.foottopbusiness.domain.repository.BookingRepository
import uz.coder.foottopbusiness.domain.usecase.booking.CreateBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsUseCase
import uz.coder.foottopbusiness.domain.usecase.owner.CreateStadiumBlockUseCase
import uz.coder.foottopbusiness.domain.usecase.owner.DeleteStadiumBlockUseCase
import uz.coder.foottopbusiness.domain.usecase.owner.GetStadiumBlocksUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase
import uz.coder.foottopbusiness.domain.usecase.user.UserIdUseCase
import uz.coder.foottopbusiness.presentation.main.booking.components.BookingOptions
import uz.coder.foottopbusiness.presentation.main.home.slots.durationMinutes
import kotlin.time.Clock

private const val DAY_COUNT = 7
/** Bir kunning bronlarini sahifalab olishda xavfsizlik chegarasi. */
private const val MAX_PAGES = 20

private fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/** Egasining kuni hamma maydonlar bo'yicha: bronlar, yopiq vaqt va bo'sh joylar. */
class ScheduleViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
    private val getBookingsUseCase: GetBookingsUseCase,
    private val getStadiumBlocksUseCase: GetStadiumBlocksUseCase,
    private val createStadiumBlockUseCase: CreateStadiumBlockUseCase,
    private val deleteStadiumBlockUseCase: DeleteStadiumBlockUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val userIdUseCase: UserIdUseCase
) : BaseViewModel<ScheduleContract.State, ScheduleContract.Effect, ScheduleContract.Event>(
    initialState = now().date.let { today ->
        ScheduleContract.State(
            days = List(DAY_COUNT) { today.plus(it, DateTimeUnit.DAY) },
            date = today
        )
    }
) {
    private var loadJob: Job? = null

    init {
        load()
    }

    override fun handleEvent(event: ScheduleContract.Event) {
        when (event) {
            is ScheduleContract.Event.SelectDate -> {
                updateState { copy(date = event.date) }
                load()
            }
            ScheduleContract.Event.Refresh -> load()

            is ScheduleContract.Event.TapLane -> onTap(event.laneId, event.minute)
            ScheduleContract.Event.DismissPick -> updateState { copy(pick = null) }
            is ScheduleContract.Event.SelectDuration -> updateState { copy(durationKey = event.key) }

            ScheduleContract.Event.StartBooking -> updateState {
                copy(
                    bookingFor = pick,
                    pick = null,
                    bookerName = "",
                    bookerPhone = "",
                    bookingOptions = BookingOptions()
                )
            }
            ScheduleContract.Event.DismissBooking -> updateState { copy(bookingFor = null) }
            is ScheduleContract.Event.UpdateBookerName -> updateState { copy(bookerName = event.name) }
            is ScheduleContract.Event.UpdateBookerPhone -> updateState { copy(bookerPhone = event.phone) }
            is ScheduleContract.Event.UpdateBookingOptions -> updateState { copy(bookingOptions = event.options) }
            ScheduleContract.Event.SubmitBooking -> submitBooking()

            ScheduleContract.Event.StartBlock -> startBlock()
            ScheduleContract.Event.DismissBlock -> updateState { copy(blockForm = null) }
            is ScheduleContract.Event.UpdateBlockForm -> updateState { copy(blockForm = event.form) }
            ScheduleContract.Event.SubmitBlock -> submitBlock()

            is ScheduleContract.Event.OpenUnblock -> updateState { copy(unblock = event.block) }
            ScheduleContract.Event.DismissUnblock -> updateState { copy(unblock = null) }
            ScheduleContract.Event.ConfirmUnblock -> removeBlock()
        }
    }

    private fun load() {
        loadJob?.cancel()
        val date = state.value.date.toString()
        loadJob = executeAsync(
            onLoading = { updateState { copy(isLoading = true, error = null) } },
            block = {
                coroutineScope {
                    val stadiums = async {
                        getStadiumsUseCase(size = 100).first().content.orEmpty()
                            .filter { it.id != null && it.isActive != false }
                    }
                    val bookings = async { bookingsOfDay(date) }
                    val blocks = async { getStadiumBlocksUseCase(date, date).first() }
                    Triple(stadiums.await(), bookings.await(), blocks.await())
                }
            },
            onSuccess = { (stadiums, bookings, blocks) ->
                updateState {
                    copy(
                        isLoading = false,
                        lanes = stadiums.map(::laneOf),
                        bookings = bookings.filter { it.status != "CANCELLED" && it.status != "REJECTED" },
                        blocks = blocks
                    )
                }
            },
            onError = { e -> updateState { copy(isLoading = false, error = e.message.orEmpty()) } }
        )
    }

    /** Shu kuni boshlanadigan hamma bronlar (barcha sahifalar). */
    private suspend fun bookingsOfDay(date: String): List<BookingResponseDto> {
        val all = mutableListOf<BookingResponseDto>()
        for (page in 0 until MAX_PAGES) {
            val chunk = getBookingsUseCase(startDateFrom = date, startDateTo = date, page = page).first()
            all += chunk
            if (chunk.size < BookingRepository.DEFAULT_PAGE_SIZE) break
        }
        return all
    }

    private fun isPast(endMinute: Int): Boolean {
        val tz = TimeZone.currentSystemDefault()
        return state.value.date.at(endMinute).toInstant(tz) <= Clock.System.now()
    }

    private fun onTap(laneId: Long, tapped: Int) {
        val s = state.value
        val lane = s.lane(laneId) ?: return
        val minute = tapped / SNAP * SNAP
        if (minute < lane.open || minute + SNAP > lane.close) return
        if (isPast(minute + SNAP)) return
        if (taken(lane, s.date, s.bookings, s.blocks, minute, minute + SNAP)) return
        updateState { copy(pick = Pick(laneId, minute), durationKey = "SIXTY") }
    }

    /** Tanlangan davomiylik bo'sh joyga sig'adimi. */
    fun pickFits(s: ScheduleContract.State = state.value): Boolean {
        val pick = s.pick ?: return false
        val lane = s.lane(pick.laneId) ?: return false
        val end = pick.start + durationMinutes(s.durationKey)
        return end <= lane.close && !taken(lane, s.date, s.bookings, s.blocks, pick.start, end)
    }

    private fun submitBooking() {
        val s = state.value
        val pick = s.bookingFor ?: return
        val name = s.bookerName.trim()
        if (s.isSubmitting || name.isEmpty() || s.bookerPhone.length != 9) return
        val end = pick.start + durationMinutes(s.durationKey)
        executeAsync(
            onLoading = { updateState { copy(isSubmitting = true) } },
            block = {
                createBookingUseCase(
                    BookingRequestDto(
                        userId = userIdUseCase(),
                        stadiumId = pick.laneId,
                        startTime = isoLocal(s.date.at(pick.start)),
                        endTime = isoLocal(s.date.at(end)),
                        // Narx yuborilmaydi: backend slotni o'zi narxlaydi (peak soatlar ham)
                        totalPrice = null,
                        status = "PENDING",
                        paymentMethod = "CASH",
                        paymentTiming = s.bookingOptions.paymentTiming,
                        bookingType = s.bookingOptions.bookingType,
                        recurrenceCount = s.bookingOptions.recurrenceCountOrNull,
                        name = name,
                        phone = s.bookerPhone
                    )
                ).first()
            },
            onSuccess = {
                updateState { copy(isSubmitting = false, bookingFor = null) }
                sendEffect(ScheduleContract.Effect.BookingCreated(name))
                load()
            },
            onError = ::onError
        )
    }

    private fun startBlock() {
        val s = state.value
        val pick = s.pick ?: return
        val lane = s.lane(pick.laneId) ?: return
        val to = minOf(pick.start + durationMinutes(s.durationKey), lane.close)
        updateState {
            copy(pick = null, blockForm = BlockForm(stadiumId = lane.id, from = hm(pick.start), to = hm(to)))
        }
    }

    private fun submitBlock() {
        val s = state.value
        val form = s.blockForm ?: return
        val lane = s.lane(form.stadiumId) ?: return
        if (s.isSubmitting) return
        var start = if (form.wholeDay) lane.open else toMinutes(form.from) ?: return
        var end = if (form.wholeDay) lane.close else toMinutes(form.to) ?: return
        // Ochilishdan oldingi vaqt - o'sha kechaning davomi (masalan 01:00 gacha)
        if (!form.wholeDay && start < lane.open && lane.close > 24 * 60) start += 24 * 60
        if (!form.wholeDay && end <= start) end += 24 * 60
        var startAt = s.date.at(start)
        // Bugungi "butun kun" hozirdan boshlanadi, o'tgan vaqtdan emas
        if (form.wholeDay) {
            val n = now()
            val nowMinute = minutesFrom(s.date, n)
            val ceil = (nowMinute + SNAP - 1) / SNAP * SNAP
            if (start < ceil) startAt = s.date.at(ceil)
        }
        executeAsync(
            onLoading = { updateState { copy(isSubmitting = true) } },
            block = {
                createStadiumBlockUseCase(
                    StadiumBlockRequestDto(
                        stadiumId = form.stadiumId,
                        startTime = isoLocal(startAt),
                        endTime = isoLocal(s.date.at(end)),
                        reason = form.reason.trim().ifEmpty { null }
                    )
                ).first()
            },
            onSuccess = {
                updateState { copy(isSubmitting = false, blockForm = null) }
                sendEffect(ScheduleContract.Effect.BlockCreated)
                load()
            },
            onError = ::onError
        )
    }

    private fun removeBlock() {
        val block = state.value.unblock ?: return
        if (state.value.isSubmitting) return
        executeAsync(
            onLoading = { updateState { copy(isSubmitting = true) } },
            block = { deleteStadiumBlockUseCase(block.id).first() },
            onSuccess = {
                updateState { copy(isSubmitting = false, unblock = null) }
                sendEffect(ScheduleContract.Effect.BlockDeleted)
                load()
            },
            onError = ::onError
        )
    }

    private fun onError(e: Throwable) {
        updateState { copy(isSubmitting = false) }
        sendEffect(ScheduleContract.Effect.ShowError(e.message.orEmpty()))
    }
}

/** Shu kun uchun "hozir" - kun boshidan daqiqada (boshqa kunda null). */
fun nowMinuteOn(day: LocalDate): Int? {
    val n = now()
    return if (n.date == day) n.hour * 60 + n.minute else null
}

fun LocalDate.isToday(): Boolean = this == now().date
