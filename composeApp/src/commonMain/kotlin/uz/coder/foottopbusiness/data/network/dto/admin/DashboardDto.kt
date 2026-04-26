package uz.coder.foottopbusiness.data.network.dto.admin


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    @SerialName("activeStadiumsCount")
    val activeStadiumsCount: Int? = null,
    @SerialName("stadiumRevenues")
    val stadiumRevenues: List<StadiumRevenue?>? = null,
    @SerialName("tournamentsCount")
    val tournamentsCount: Int? = null,
    @SerialName("usersCount")
    val usersCount: Int? = null
)