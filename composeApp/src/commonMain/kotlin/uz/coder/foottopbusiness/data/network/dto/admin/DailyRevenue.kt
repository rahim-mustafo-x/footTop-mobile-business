package uz.coder.foottopbusiness.data.network.dto.admin


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyRevenue(
    @SerialName("date")
    val date: String? = null,
    @SerialName("revenue")
    val revenue: Int? = null
)