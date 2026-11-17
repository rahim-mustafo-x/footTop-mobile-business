package uz.coder.foottopbusiness.data.network.dto.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uz.coder.foottopbusiness.core.NetworkConfig
import uz.coder.foottopbusiness.core.Serializable as KmpSerializable

@Serializable
data class CreateStadiumRequest(
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("ownerId") val ownerId: Int? = null,
    @SerialName("regionId") val regionId: Int,
    @SerialName("districtId") val districtId: Int,
    @SerialName("description") val description: String,
    @SerialName("location") val location: LocationDto,
    @SerialName("type") val type: String,
    @SerialName("duration") val duration: String,
    @SerialName("capacity") val capacity: Int,
    @SerialName("pricePerHour") val pricePerHour: Int,
    @SerialName("images") val images: List<ImageDto>,
    @SerialName("isActive") val isActive: Boolean = true,
    /** Gavjum soat narxi; 0 - o'chirish, null - saqlangan qiymat o'zgarmaydi. */
    @SerialName("peakPricePerHour") val peakPricePerHour: Int? = null,
    /** "18:00:00" */
    @SerialName("peakStartTime") val peakStartTime: String? = null,
    @SerialName("peakEndTime") val peakEndTime: String? = null,
    /** Shanba-yakshanba kun bo'yi gavjum narx. */
    @SerialName("peakWeekends") val peakWeekends: Boolean? = null,
) : KmpSerializable {
    fun withPeak(peak: PeakPricing?): CreateStadiumRequest = if (peak == null) this else copy(
        peakPricePerHour = peak.pricePerHour,
        peakStartTime = peak.startTime?.let { "$it:00" },
        peakEndTime = peak.endTime?.let { "$it:00" },
        peakWeekends = peak.weekends
    )
}

/** Formadagi gavjum narx: vaqtlar "HH:mm", narx 0 bo'lsa gavjum narx yo'q. */
data class PeakPricing(
    val pricePerHour: Int,
    val startTime: String?,
    val endTime: String?,
    val weekends: Boolean
)

@Serializable
data class LocationDto(
    @SerialName("latitude") val latitude: Double? = 0.0,
    @SerialName("longitude") val longitude: Double? = 0.0,
    @SerialName("address") val address: String? = null,
) : KmpSerializable

@Serializable
data class ImageDto(
    @SerialName("urls") val urls: String,
) : KmpSerializable

/**
 * Rasmni yuklash uchun to'liq manzil. Server nisbiy yo'l qaytaradi
 * (`/v1/files/stadiums/<uuid>.jpg`), eski yozuvlarda esa to'liq URL bo'lishi mumkin.
 */
val ImageDto.fullUrl: String
    get() = if (urls.startsWith("http://") || urls.startsWith("https://")) {
        urls
    } else {
        NetworkConfig.BASE_URL.trimEnd('/') + "/" + urls.trimStart('/')
    }
