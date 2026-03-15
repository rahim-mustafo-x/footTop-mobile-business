package uz.coder.foottopbusiness.data.network.dto.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegionDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("shortName") val shortName: String = "",
)

@Serializable
data class DistrictDto(
    @SerialName("id") val id: Int? = 0,
    @SerialName("name") val name: String? = "",
    @SerialName("shortName") val shortName: String? = "",
    @SerialName("provinceId") val provinceId: Int? = 0,
    @SerialName("regionId") val regionId: Int? = 0,
)
