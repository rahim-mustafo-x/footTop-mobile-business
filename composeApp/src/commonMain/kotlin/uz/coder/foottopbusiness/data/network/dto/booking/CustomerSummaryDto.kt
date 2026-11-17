package uz.coder.foottopbusiness.data.network.dto.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Bitta telefon raqam bo'yicha xodim ko'ra oladigan hamma bronlar xulosasi. */
@Serializable
data class CustomerSummaryDto(
    @SerialName("phone") val phone: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("totalBookings") val totalBookings: Int = 0,
    @SerialName("playedCount") val playedCount: Int = 0,
    @SerialName("upcomingCount") val upcomingCount: Int = 0,
    @SerialName("cancelledCount") val cancelledCount: Int = 0,
    @SerialName("noShowCount") val noShowCount: Int = 0,
    @SerialName("unpaidCount") val unpaidCount: Int = 0,
    @SerialName("unpaidAmount") val unpaidAmount: Double = 0.0,
    @SerialName("paidAmount") val paidAmount: Double = 0.0,
    @SerialName("firstBookingAt") val firstBookingAt: String? = null,
    @SerialName("lastBookingAt") val lastBookingAt: String? = null,
    @SerialName("recent") val recent: List<BookingResponseDto>? = null
)
