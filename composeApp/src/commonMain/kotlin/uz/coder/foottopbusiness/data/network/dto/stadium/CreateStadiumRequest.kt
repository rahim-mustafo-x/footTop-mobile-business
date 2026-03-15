package uz.coder.foottopbusiness.data.network.dto.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateStadiumRequest(
    @SerialName("name") val name: String,
    @SerialName("ownerId") val ownerId: Int,
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
    @SerialName("openTime") val openTime: String,
    @SerialName("closeTime") val closeTime: String,
)

@Serializable
data class LocationDto(
    @SerialName("latitude") val latitude: Double? = 0.0,
    @SerialName("longitude") val longitude: Double? = 0.0,
)

@Serializable
data class ImageDto(
    @SerialName("urls") val urls: String,
)
