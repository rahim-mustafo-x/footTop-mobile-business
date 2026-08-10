package uz.coder.foottopbusiness.data.network.dto.tournament

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import uz.coder.foottopbusiness.data.network.dto.stadium.LocationDto

@Serializable
data class TournamentRequestDto(
    @SerialName("name") val name: String,
    @SerialName("organizerId") val organizerId: Long,
    @SerialName("districtId") val districtId: Long? = null,
    // Turnir o'tadigan asosiy stadion. Turnir bir nechta stadionda o'tishi mumkin,
    // lekin backend hozircha bittasini saqlaydi - ilova asosiysini yuboradi.
    @SerialName("stadiumId") val stadiumId: Long? = null,
    @SerialName("startDate") val startDate: String,       // "yyyy-MM-dd"
    @SerialName("endDate") val endDate: String,           // "yyyy-MM-dd"
    @SerialName("sportType") val sportType: String = "FOOTBALL",
    @SerialName("maxTeams") val maxTeams: Int,
    @SerialName("entryFee") val entryFee: Long = 0,
    @SerialName("status") val status: String = "UPCOMING",
    @SerialName("address") val address: String? = null,
    @SerialName("startTime") val startTime: String? = null, // "HH:mm"
    @SerialName("endTime") val endTime: String? = null,     // "HH:mm"
    @SerialName("prizes") val prizes: String? = null,
    @SerialName("rules") val rules: String? = null,
    @SerialName("location") val location: LocationDto? = null,
)
