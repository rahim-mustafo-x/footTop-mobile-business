package uz.coder.foottopbusiness.data.network.dto.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * /v1/admin/dashboard/home javobi. Backend uni chaqiruvchining doirasida
 * qaytaradi: ega - o'z stadionlari, tuman admini - o'z tumani, super admin - hammasi.
 */
@Serializable
data class HomeDto(
    @SerialName("date") val date: String? = null,
    @SerialName("todayBookingsCount") val todayBookingsCount: Int? = null,
    @SerialName("finishedCount") val finishedCount: Int? = null,
    @SerialName("upcomingCount") val upcomingCount: Int? = null,
    @SerialName("todayRevenue") val todayRevenue: Double? = null,
    @SerialName("monthRevenue") val monthRevenue: Double? = null,
    @SerialName("activeStadiumsCount") val activeStadiumsCount: Int? = null,
    @SerialName("tournamentsCount") val tournamentsCount: Int? = null,
    @SerialName("usersCount") val usersCount: Int? = null,
    @SerialName("nextBooking") val nextBooking: HomeBookingDto? = null,
    @SerialName("todaySchedule") val todaySchedule: List<HomeBookingDto>? = null
)

@Serializable
data class HomeBookingDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("stadiumId") val stadiumId: Long? = null,
    @SerialName("stadiumName") val stadiumName: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("durationMinutes") val durationMinutes: Int? = null,
    @SerialName("totalPrice") val totalPrice: Double? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("paymentStatus") val paymentStatus: String? = null
)
