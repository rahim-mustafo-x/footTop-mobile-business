package uz.coder.foottopbusiness.data.network.dto.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uz.coder.foottopbusiness.core.Serializable as KmpSerializable

@Serializable
data class StadiumResponse(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("ownerId") val ownerId: Int? = null,
    @SerialName("ownerName") val ownerName: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("location") val location: LocationDto? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("capacity") val capacity: Int? = null,
    @SerialName("pricePerHour") val pricePerHour: Double? = null,
    @SerialName("isActive") val isActive: Boolean? = null,
    @SerialName("districtId") val districtId: Int? = null,
    @SerialName("districtName") val districtName: String? = null,
    @SerialName("regionId") val regionId: Int? = null,
    @SerialName("regionName") val regionName: String? = null,
    @SerialName("images") val images: List<ImageDto>? = null,
    @SerialName("isFavorite") val isFavorite: Boolean? = null,
    @SerialName("openTime") val openTime: String? = null,
    @SerialName("closeTime") val closeTime: String? = null,
    @SerialName("slots") val slots: List<SlotDto>? = null,
    @SerialName("earliestAvailable") val earliestAvailable: String? = null,
) : KmpSerializable

@Serializable
data class SlotDto(
    @SerialName("start") val start: String? = null,
    @SerialName("end") val end: String? = null,
    @SerialName("status") val status: String? = null,
) : KmpSerializable
