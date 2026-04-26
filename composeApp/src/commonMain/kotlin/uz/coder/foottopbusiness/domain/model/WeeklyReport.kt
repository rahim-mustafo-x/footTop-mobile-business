package uz.coder.foottopbusiness.domain.model

data class WeeklyReport(
    val bookingsGrowthPercent: Double,
    val dailyRevenue: List<DailyRevenue>,
    val previousWeekBookings: Int,
    val totalBookings: Int,
    val weekEnd: String,
    val weekStart: String,
    val weeklyRevenue: Double
)