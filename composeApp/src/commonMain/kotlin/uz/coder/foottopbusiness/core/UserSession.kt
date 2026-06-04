package uz.coder.foottopbusiness.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.domain.model.UserRole

class UserSession {
    private val _user = MutableStateFlow<UserDto?>(null)
    val user = _user.asStateFlow()

    private val _role = MutableStateFlow(UserRole.UNKNOWN)
    val role = _role.asStateFlow()

    fun setUser(user: UserDto?) {
        _user.value = user
        _role.value = if (user != null) determineRole(user) else UserRole.UNKNOWN
    }

    private fun determineRole(user: UserDto): UserRole {
        val isSuperAdmin = user.roles?.any { it.name == "ROLE_SUPER_ADMIN" || it.name == "SUPER_ADMIN" } ?: false
        val isDistrictAdmin = user.roles?.any { it.name == "ROLE_DISTRICT_ADMIN" } ?: false
        val isOwner = user.roles?.any { it.name == "ROLE_OWNER" } ?: false
        
        return when {
            isSuperAdmin -> UserRole.SUPER_ADMIN
            isDistrictAdmin -> UserRole.DISTRICT_ADMIN
            isOwner -> UserRole.OWNER
            user.roles?.any { it.name?.contains("COACH", ignoreCase = true) == true || it.name?.contains("MURABBIY", ignoreCase = true) == true } == true -> UserRole.COACH
            else -> UserRole.fromString(user.roles?.firstOrNull()?.name)
        }
    }
    
    fun clear() {
        _user.value = null
        _role.value = UserRole.UNKNOWN
    }
}
