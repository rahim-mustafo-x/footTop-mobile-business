package uz.coder.foottopbusiness.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoachResponseDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("coachName") val coachName: String? = null,
    @SerialName("specialty") val specialty: String? = null,
    @SerialName("experienceYears") val experienceYears: Int? = null,
    @SerialName("hourlyRate") val hourlyRate: Double? = null,
    @SerialName("availability") val availability: String? = null,
    @SerialName("reviews") val reviews: String? = null,
)
