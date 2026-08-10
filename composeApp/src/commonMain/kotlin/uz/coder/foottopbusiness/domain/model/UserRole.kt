package uz.coder.foottopbusiness.domain.model

enum class UserRole(val roleName: String) {
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    DISTRICT_ADMIN("ROLE_DISTRICT_ADMIN"),
    OWNER("ROLE_OWNER"),
    COACH("ROLE_COACH"),
    PLAYER("ROLE_PLAYER"),
    UNKNOWN("");

    companion object {
        /**
         * Vakolat ustuvorligi: bitta foydalanuvchida bir nechta rol bo'lsa,
         * shu tartibda birinchi mos kelgani tanlanadi.
         */
        private val Priority = listOf(SUPER_ADMIN, DISTRICT_ADMIN, OWNER, COACH, PLAYER)

        /**
         * Rol satrini enum'ga o'giradi.
         *
         * Backend hozir "ROLE_SUPER_ADMIN" va "ROLE_OWNER" qaytaradi, lekin
         * prefikssiz ("SUPER_ADMIN") va muqobil nomlar ("EGASI", "MURABBIY")
         * ham qabul qilinadi - ilgari bu variantlar ikki xil joyda ikki xil
         * qayta ishlanib, mos kelmay qolar edi.
         */
        fun fromString(role: String?): UserRole {
            val normalized = role?.trim()?.uppercase()?.removePrefix("ROLE_").orEmpty()
            if (normalized.isEmpty()) return UNKNOWN

            return when {
                normalized.contains("SUPER") && normalized.contains("ADMIN") -> SUPER_ADMIN
                normalized.contains("DISTRICT") -> DISTRICT_ADMIN
                normalized.contains("OWNER") || normalized.contains("EGASI") -> OWNER
                normalized.contains("COACH") || normalized.contains("MURABBIY") -> COACH
                normalized.contains("PLAYER") -> PLAYER
                // Oddiy "ADMIN" - tuman admini deb qaraladi.
                // Backend boshqa ma'no bersa, shu qatorni o'zgartirish kifoya.
                normalized.contains("ADMIN") -> DISTRICT_ADMIN
                else -> UNKNOWN
            }
        }

        /**
         * Rollar ro'yxatidan eng yuqori vakolatlisini tanlaydi.
         *
         * Ro'yxat tartibiga tayanib bo'lmaydi - API uni ixtiyoriy tartibda
         * qaytarishi mumkin, shuning uchun [Priority] bo'yicha saralaymiz.
         */
        fun highestOf(roles: List<String?>?): UserRole {
            if (roles.isNullOrEmpty()) return UNKNOWN
            val resolved = roles.map { fromString(it) }.toSet()
            return Priority.firstOrNull { it in resolved } ?: UNKNOWN
        }
    }
}
