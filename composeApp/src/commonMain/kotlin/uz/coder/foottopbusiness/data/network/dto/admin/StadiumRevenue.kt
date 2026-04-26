package uz.coder.foottopbusiness.data.network.dto.admin


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumRevenue(
    @SerialName("stadiumId")
    val stadiumId: Int? = null,
    @SerialName("totalRevenue")
    val totalRevenue: Double? = null
)