package uz.coder.foottopbusiness.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("fullName") val fullName: String? = null,
    @SerialName("profileImageUrl") val profileImageUrl: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("districtId") val districtId: Long? = null,
    @SerialName("districtName") val districtName: String? = null,
    @SerialName("roles") val roles: List<RoleDto>? = null,
)

@Serializable
data class RoleDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String? = null,
)
