package uz.coder.foottopbusiness.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import uz.coder.foottopbusiness.data.network.dto.stadium.LocationDto

@Serializable
data class TimeDto(
    @SerialName("hour") val hour: Int? = 0,
    @SerialName("minute") val minute: Int? = 0,
    @SerialName("second") val second: Int? = 0,
    @SerialName("nano") val nano: Int? = 0
) {
    fun toFormattedString(): String {
        val h = hour?.toString()?.padStart(2, '0') ?: "00"
        val m = minute?.toString()?.padStart(2, '0') ?: "00"
        return "$h:$m"
    }
}

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
    @SerialName("location") val location: LocationDto? = null,
)
