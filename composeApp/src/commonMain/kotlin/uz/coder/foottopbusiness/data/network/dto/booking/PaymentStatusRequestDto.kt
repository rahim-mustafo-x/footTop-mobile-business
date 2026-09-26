package uz.coder.foottopbusiness.data.network.dto.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusRequestDto(
    /** PAID yoki UNPAID. */
    @SerialName("paymentStatus") val paymentStatus: String
)
