package uz.coder.foottopbusiness.data.network.dto.booking

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BookingResponseDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("userId") val userId: Long? = null,
    @SerialName("stadiumId") val stadiumId: Long? = null,
    @SerialName("matchId") val matchId: Long? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("totalPrice") val totalPrice: Double? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("paymentMethod") val paymentMethod: String? = null
)
