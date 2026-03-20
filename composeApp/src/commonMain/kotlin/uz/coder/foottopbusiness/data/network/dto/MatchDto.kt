package uz.coder.foottopbusiness.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchResponseDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("organizerId") val organizerId: Long? = null,
    @SerialName("stadiumId") val stadiumId: Long? = null,
    @SerialName("dateTime") val dateTime: String? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("maxPlayers") val maxPlayers: Int? = null,
    @SerialName("currentPlayers") val currentPlayers: Int? = null,
    @SerialName("pricePerPlayer") val pricePerPlayer: Double? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("sportType") val sportType: String? = null,
)
