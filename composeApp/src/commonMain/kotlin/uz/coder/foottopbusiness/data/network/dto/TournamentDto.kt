package uz.coder.foottopbusiness.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentResponseDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("organizerId") val organizerId: Long? = null,
    @SerialName("startDate") val startDate: String? = null,
    @SerialName("endDate") val endDate: String? = null,
    @SerialName("sportType") val sportType: String? = null,
    @SerialName("maxTeams") val maxTeams: Int? = null,
    @SerialName("teamApplied") val teamApplied: Int? = null,
    @SerialName("entryFee") val entryFee: Double? = null,
    @SerialName("rules") val rules: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("prizes") val prizes: String? = null,
)
