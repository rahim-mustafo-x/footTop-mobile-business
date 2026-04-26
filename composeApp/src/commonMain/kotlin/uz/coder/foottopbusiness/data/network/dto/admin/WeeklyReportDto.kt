package uz.coder.foottopbusiness.data.network.dto.admin


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeeklyReportDto(
    @SerialName("bookingsGrowthPercent")
    val bookingsGrowthPercent: Double? = null,
    @SerialName("dailyRevenue")
    val dailyRevenue: List<DailyRevenue?>? = null,
    @SerialName("previousWeekBookings")
    val previousWeekBookings: Int? = null,
    @SerialName("totalBookings")
    val totalBookings: Int? = null,
    @SerialName("weekEnd")
    val weekEnd: String? = null,
    @SerialName("weekStart")
    val weekStart: String? = null,
    @SerialName("weeklyRevenue")
    val weeklyRevenue: Double? = null
)