package uz.coder.foottopbusiness.data.network.dto.tournament

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentFilterDto(
    @SerialName("name") val name: String? = null,
    @SerialName("organizerId") val organizerId: Long? = null,
    @SerialName("startDateFrom") val startDateFrom: String? = null,
    @SerialName("startDateTo") val startDateTo: String? = null,
    @SerialName("sportType") val sportType: String? = null,
    @SerialName("maxTeams") val maxTeams: Int? = null,
    @SerialName("maxEntryFee") val maxEntryFee: Double? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("address") val address: String? = null,
)
