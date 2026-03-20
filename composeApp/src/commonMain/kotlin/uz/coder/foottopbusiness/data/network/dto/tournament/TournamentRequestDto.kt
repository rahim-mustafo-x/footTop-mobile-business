package uz.coder.foottopbusiness.data.network.dto.tournament

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentRequestDto(
    @SerialName("name") val name: String,
    @SerialName("organizerId") val organizerId: Long,
    @SerialName("startDate") val startDate: String,       // "yyyy-MM-dd"
    @SerialName("endDate") val endDate: String,           // "yyyy-MM-dd"
    @SerialName("sportType") val sportType: String = "FOOTBALL",
    @SerialName("maxTeams") val maxTeams: Int,
    @SerialName("entryFee") val entryFee: Double = 0.0,
    @SerialName("status") val status: String = "UPCOMING",
    @SerialName("address") val address: String? = null,
    @SerialName("prizes") val prizes: String? = null,
    @SerialName("rules") val rules: String? = null,
)
