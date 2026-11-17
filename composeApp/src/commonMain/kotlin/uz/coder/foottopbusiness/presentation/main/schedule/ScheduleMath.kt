package uz.coder.foottopbusiness.presentation.main.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import uz.coder.foottopbusiness.core.minutesBetween
import uz.coder.foottopbusiness.core.plusMinutes
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

/** Jadval qadami: bo'sh joy shu daqiqalarga yaxlitlanadi. */
const val SNAP = 30
private const val DAY = 24 * 60

/** Bitta maydon ustuni: ochilish/yopilish kun boshidan daqiqada (yopilish keyingi kunga o'tishi mumkin). */
data class Lane(val stadium: StadiumResponse, val open: Int, val close: Int) {
    val id: Long get() = stadium.id!!.toLong()
}

data class LaneBooking(val booking: BookingResponseDto, val start: Int, val end: Int)
data class LaneBlock(val block: StadiumBlockDto, val start: Int, val end: Int)

/** "08:30" yoki "2026-10-23T08:30:00" -> 510 */
fun toMinutes(hm: String?): Int? {
    val t = hm?.substringAfter('T')?.take(5) ?: return null
    val h = t.substringBefore(':').toIntOrNull() ?: return null
    val m = t.substringAfter(':', "0").toIntOrNull() ?: 0
    return h * 60 + m
}

/** 1530 -> "01:30" (keyingi kun aylanadi) */
fun hm(minutes: Int): String {
    val m = ((minutes % DAY) + DAY) % DAY
    return "${(m / 60).toString().padStart(2, '0')}:${(m % 60).toString().padStart(2, '0')}"
}

fun laneOf(stadium: StadiumResponse): Lane {
    val open = toMinutes(stadium.openTime) ?: (8 * 60)
    var close = toMinutes(stadium.closeTime) ?: (22 * 60)
    if (close <= open) close += DAY
    return Lane(stadium, open, close)
}

fun LocalDate.startOfDay(): LocalDateTime = atTime(LocalTime(0, 0))

/** Kun yarim tunidan beri o'tgan daqiqalar (yarim tundan keyingi vaqt 24 soatdan oshadi). */
fun minutesFrom(day: LocalDate, at: LocalDateTime): Int = minutesBetween(day.startOfDay(), at)

fun LocalDate.at(minutes: Int): LocalDateTime = startOfDay().plusMinutes(minutes)

/** Backendga ketadigan mahalliy vaqt: 2026-10-23T08:30:00 */
fun isoLocal(t: LocalDateTime): String {
    val two = { n: Int -> n.toString().padStart(2, '0') }
    return "${t.date}T${two(t.hour)}:${two(t.minute)}:00"
}

fun bookingsOf(lane: Lane, day: LocalDate, bookings: List<BookingResponseDto>): List<LaneBooking> =
    bookings.mapNotNull { b ->
        val s = b.startTime.toLocalDateTimeSafe() ?: return@mapNotNull null
        val e = b.endTime.toLocalDateTimeSafe() ?: return@mapNotNull null
        if (b.stadiumId != lane.id) null else LaneBooking(b, minutesFrom(day, s), minutesFrom(day, e))
    }

/** Bir necha kunga cho'zilgan yopiq vaqt ko'rinadigan oraliqqa qirqiladi. */
fun blocksOf(lane: Lane, day: LocalDate, blocks: List<StadiumBlockDto>, rangeStart: Int, rangeEnd: Int): List<LaneBlock> =
    blocks.mapNotNull { b ->
        val s = b.startTime.toLocalDateTimeSafe() ?: return@mapNotNull null
        val e = b.endTime.toLocalDateTimeSafe() ?: return@mapNotNull null
        if (b.stadiumId != lane.id) null
        else LaneBlock(b, maxOf(rangeStart, minutesFrom(day, s)), minOf(rangeEnd, minutesFrom(day, e)))
    }

/** [start, end) oralig'ida bron yoki yopiq vaqt bormi. */
fun taken(lane: Lane, day: LocalDate, bookings: List<BookingResponseDto>, blocks: List<StadiumBlockDto>, start: Int, end: Int): Boolean =
    bookingsOf(lane, day, bookings).any { start < it.end && end > it.start } ||
        blocks.any { b ->
            val s = b.startTime.toLocalDateTimeSafe() ?: return@any false
            val e = b.endTime.toLocalDateTimeSafe() ?: return@any false
            b.stadiumId == lane.id && start < minutesFrom(day, e) && end > minutesFrom(day, s)
        }
