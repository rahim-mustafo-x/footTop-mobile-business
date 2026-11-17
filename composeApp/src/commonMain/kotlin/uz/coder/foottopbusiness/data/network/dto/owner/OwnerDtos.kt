package uz.coder.foottopbusiness.data.network.dto.owner

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

/** Stadionning yopilgan vaqti (ta'mir, tadbir...): bu oraliqda bron qilib bo'lmaydi. */
@Serializable
data class StadiumBlockDto(
    @SerialName("id") val id: Long,
    @SerialName("stadiumId") val stadiumId: Long,
    @SerialName("stadiumName") val stadiumName: String? = null,
    @SerialName("startTime") val startTime: String,
    @SerialName("endTime") val endTime: String,
    @SerialName("reason") val reason: String? = null,
    @SerialName("createdAt") val createdAt: String? = null
)

@Serializable
data class StadiumBlockRequestDto(
    @SerialName("stadiumId") val stadiumId: Long,
    @SerialName("startTime") val startTime: String,
    @SerialName("endTime") val endTime: String,
    @SerialName("reason") val reason: String? = null
)

/** Davr bo'yicha bronlar asosidagi hisobot (ko'pi bilan 92 kun). */
@Serializable
data class OwnerReportDto(
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("bookingCount") val bookingCount: Int = 0,
    @SerialName("confirmedCount") val confirmedCount: Int = 0,
    @SerialName("pendingCount") val pendingCount: Int = 0,
    @SerialName("cancelledCount") val cancelledCount: Int = 0,
    @SerialName("rejectedCount") val rejectedCount: Int = 0,
    @SerialName("noShowCount") val noShowCount: Int = 0,
    @SerialName("revenue") val revenue: Double = 0.0,
    @SerialName("paidAmount") val paidAmount: Double = 0.0,
    @SerialName("unpaidAmount") val unpaidAmount: Double = 0.0,
    @SerialName("bookedHours") val bookedHours: Double = 0.0,
    @SerialName("occupancyPercent") val occupancyPercent: Double? = null,
    @SerialName("stadiums") val stadiums: List<OwnerReportStadiumDto> = emptyList(),
    @SerialName("hours") val hours: List<OwnerReportHourDto> = emptyList(),
    @SerialName("days") val days: List<OwnerReportDayDto> = emptyList(),
    @SerialName("unpaid") val unpaid: List<BookingResponseDto> = emptyList()
)

@Serializable
data class OwnerReportStadiumDto(
    @SerialName("stadiumId") val stadiumId: Long,
    @SerialName("stadiumName") val stadiumName: String? = null,
    @SerialName("bookings") val bookings: Int = 0,
    @SerialName("revenue") val revenue: Double = 0.0,
    @SerialName("bookedHours") val bookedHours: Double = 0.0,
    @SerialName("openHours") val openHours: Double = 0.0,
    @SerialName("occupancyPercent") val occupancyPercent: Double? = null
)

@Serializable
data class OwnerReportHourDto(
    @SerialName("hour") val hour: Int,
    @SerialName("bookings") val bookings: Int = 0
)

@Serializable
data class OwnerReportDayDto(
    @SerialName("date") val date: String,
    @SerialName("bookings") val bookings: Int = 0,
    @SerialName("revenue") val revenue: Double = 0.0
)

/** Stadion haqidagi o'yinchi sharhi va egasining javobi. */
@Serializable
data class StaffRatingDto(
    @SerialName("id") val id: Long,
    @SerialName("stadiumId") val stadiumId: Long? = null,
    @SerialName("stadiumName") val stadiumName: String? = null,
    @SerialName("userId") val userId: Long? = null,
    @SerialName("userFullName") val userFullName: String? = null,
    @SerialName("rating") val rating: Int? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("ownerReply") val ownerReply: String? = null,
    @SerialName("ownerReplyAt") val ownerReplyAt: String? = null
)

@Serializable
data class RatingReplyRequestDto(
    /** Bo'sh matn javobni o'chiradi. */
    @SerialName("reply") val reply: String
)
