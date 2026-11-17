package uz.coder.foottopbusiness.data.network.dto.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoShowRequestDto(
    /** true - o'yinchi kelmadi, false - belgini olib tashlash. */
    @SerialName("noShow") val noShow: Boolean
)
