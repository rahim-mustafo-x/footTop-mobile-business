package uz.coder.foottopbusiness.presentation.main.schedule

import kotlinx.datetime.LocalDate
import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockDto
import uz.coder.foottopbusiness.presentation.main.booking.components.BookingOptions

/** Bosilgan bo'sh joy: qaysi maydon va kun boshidan necha daqiqa. */
data class Pick(val laneId: Long, val start: Int)

/** "Vaqtni yopish" formasi; vaqtlar "HH:mm". */
data class BlockForm(
    val stadiumId: Long,
    val from: String,
    val to: String,
    val wholeDay: Boolean = false,
    val reason: String = ""
)

sealed interface ScheduleContract {
    data class State(
        val days: List<LocalDate>,
        val date: LocalDate,
        val isLoading: Boolean = true,
        val error: String? = null,
        val lanes: List<Lane> = emptyList(),
        /** Bekor qilingan va rad etilganlarsiz. */
        val bookings: List<BookingResponseDto> = emptyList(),
        val blocks: List<StadiumBlockDto> = emptyList(),

        val pick: Pick? = null,
        val durationKey: String = "SIXTY",

        val bookingFor: Pick? = null,
        val bookerName: String = "",
        val bookerPhone: String = "",
        val bookingOptions: BookingOptions = BookingOptions(),

        val blockForm: BlockForm? = null,
        val unblock: StadiumBlockDto? = null,
        val isSubmitting: Boolean = false
    ) : MviState {
        val rangeStart: Int get() = lanes.minOfOrNull { it.open }?.let { it / 60 * 60 } ?: (8 * 60)
        val rangeEnd: Int get() = lanes.maxOfOrNull { it.close }?.let { (it + 59) / 60 * 60 } ?: (22 * 60)
        fun lane(id: Long): Lane? = lanes.firstOrNull { it.id == id }
    }

    sealed interface Effect : MviEffect {
        data class BookingCreated(val name: String) : Effect
        object BlockCreated : Effect
        object BlockDeleted : Effect
        /** Xom xabar - ekranda ErrorMapper orqali o'giriladi. */
        data class ShowError(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        data class SelectDate(val date: LocalDate) : Event
        object Refresh : Event

        data class TapLane(val laneId: Long, val minute: Int) : Event
        object DismissPick : Event
        data class SelectDuration(val key: String) : Event

        object StartBooking : Event
        object DismissBooking : Event
        data class UpdateBookerName(val name: String) : Event
        data class UpdateBookerPhone(val phone: String) : Event
        data class UpdateBookingOptions(val options: BookingOptions) : Event
        object SubmitBooking : Event

        object StartBlock : Event
        object DismissBlock : Event
        data class UpdateBlockForm(val form: BlockForm) : Event
        object SubmitBlock : Event

        data class OpenUnblock(val block: StadiumBlockDto) : Event
        object DismissUnblock : Event
        object ConfirmUnblock : Event
    }
}
