package uz.coder.foottopbusiness.data.network.dto.tournament

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto

@Serializable
data class PageTournamentResponseDto(
    @SerialName("content") val content: List<TournamentResponseDto>? = null,
    @SerialName("totalElements") val totalElements: Long? = null,
    @SerialName("totalPages") val totalPages: Int? = null,
    @SerialName("number") val number: Int? = null,
    @SerialName("size") val size: Int? = null,
    @SerialName("last") val last: Boolean? = null,
)
