package uz.coder.foottopbusiness.core

object RoleConstants {
    const val SUPER_ADMIN = "ROLE_SUPER_ADMIN"
    const val ADMIN = "ROLE_ADMIN"
    const val STADIUM_OWNER = "ROLE_STADIUM_OWNER"
    const val OWNER = "ROLE_OWNER"
    const val PLAYER = "ROLE_PLAYER"
    const val COACH = "ROLE_COACH"
    const val MURABBIY = "ROLE_MURABBIY"
    const val EGASI = "ROLE_EGASI"

    fun isAdmin(roleName: String?): Boolean {
        return roleName?.contains("ADMIN", ignoreCase = true) == true
    }

    fun isOwner(roleName: String?): Boolean {
        return roleName?.contains("OWNER", ignoreCase = true) == true || 
               roleName?.contains("EGASI", ignoreCase = true) == true
    }
}
