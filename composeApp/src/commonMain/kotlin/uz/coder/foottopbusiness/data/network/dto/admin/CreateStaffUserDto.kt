package uz.coder.foottopbusiness.data.network.dto.admin

import kotlinx.serialization.Serializable

@Serializable
data class CreateStaffUserDto(
    val fullName: String? = null,
    val username: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val role: String? = null, // SUPER_ADMIN, DISTRICT_ADMIN, OWNER, COACH
    val districtId: Long? = null
)
