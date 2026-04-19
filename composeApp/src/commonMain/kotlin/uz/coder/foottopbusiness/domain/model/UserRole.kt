package uz.coder.foottopbusiness.domain.model

enum class UserRole(val roleName: String) {
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    DISTRICT_ADMIN("ROLE_DISTRICT_ADMIN"),
    OWNER("ROLE_OWNER"),
    COACH("ROLE_COACH"),
    PLAYER("ROLE_PLAYER"),
    UNKNOWN("");

    companion object {
        fun fromString(role: String?): UserRole {
            return entries.find { it.roleName == role } ?: UNKNOWN
        }
    }
}
