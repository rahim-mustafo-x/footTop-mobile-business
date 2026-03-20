package uz.coder.foottopbusiness.data.network.dto.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageStadiumResponseDto(
    @SerialName("content") val content: List<StadiumResponse>? = null,
    @SerialName("totalElements") val totalElements: Long? = null,
    @SerialName("totalPages") val totalPages: Int? = null,
    @SerialName("number") val number: Int? = null,
    @SerialName("size") val size: Int? = null,
    @SerialName("last") val last: Boolean? = null,
)
