package uz.coder.foottopbusiness.data.network.dto.coach

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoachRequestDto(
    @SerialName("userId") val userId: Long,
    @SerialName("specialty") val specialty: String,
    @SerialName("experienceYears") val experienceYears: Int,
    @SerialName("hourlyRate") val hourlyRate: Double,
    @SerialName("availability") val availability: String? = null,
    @SerialName("reviews") val reviews: String? = null,
    @SerialName("duration") val duration: String? = null, // SIXTY, NINETY, ONE_HUNDRED_TWENTY
)
