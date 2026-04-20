package uz.coder.foottopbusiness.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse <T> (
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("code")
    val code: String? = null,
    @SerialName("details")
    val details: List<String>? = null,
    @SerialName("data")
    val data: T? = null
)