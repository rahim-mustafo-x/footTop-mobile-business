package uz.coder.foottopbusiness.data.network.dto

import kotlinx.serialization.Serializable

// data/network/dto/UserRequestDto.kt
@Serializable
data class UserRequestDto(
    val username: String? = null,
    val password: String? = null,
    val phone: String? = null,
    val fullName: String? = null,
    val profileImageUrl: String? = null,
    val location: String? = null,
    val districtId: Long? = null,
    val roles: List<String>? = null,
)