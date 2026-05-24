package uz.coder.foottopbusiness.data.network.dto.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelBookingRequestDto(
    @SerialName("reason") val reason: String
)
