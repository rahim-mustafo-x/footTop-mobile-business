package uz.coder.foottopbusiness.data.network.dto.booking

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BookingRequestDto(
    @SerialName("userId") val userId: Long? = null,
    @SerialName("stadiumId") val stadiumId: Long? = null,
    @SerialName("matchId") val matchId: Long? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("totalPrice") val totalPrice: Double? = null,
    @SerialName("status") val status: String? = "PENDING",
    @SerialName("paymentMethod") val paymentMethod: String? = "CASH",
    /** PREPAID - oldindan, PAY_AFTER_GAME - o'yindan keyin. */
    @SerialName("paymentTiming") val paymentTiming: String? = null,
    /** ONE_TIME yoki RECURRING (har hafta shu kun/vaqtda). */
    @SerialName("bookingType") val bookingType: String? = null,
    /** Faqat RECURRING uchun: jami necha hafta (2..12). */
    @SerialName("recurrenceCount") val recurrenceCount: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null
)
