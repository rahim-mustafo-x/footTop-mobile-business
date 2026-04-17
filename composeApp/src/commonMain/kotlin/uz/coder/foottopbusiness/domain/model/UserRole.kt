package uz.coder.foottopbusiness.domain.model

enum class UserRole(val roleName: String) {
    ADMIN("ROLE_ADMIN"),
    OWNER("ROLE_STADIUM_OWNER"),
    COACH("ROLE_COACH"),
    USER("ROLE_USER"),
    UNKNOWN("");

    companion object {
        fun fromString(role: String?): UserRole {
            return entries.find { it.roleName == role } ?: UNKNOWN
        }
    }
}
