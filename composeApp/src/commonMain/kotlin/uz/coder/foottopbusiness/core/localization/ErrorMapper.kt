package uz.coder.foottopbusiness.core.localization

/**
 * Backend'dan kelgan xato kodlarini foydalanuvchi tiliga o'giradi.
 * Mos kod topilmasa, xom xabar o'zgarishsiz qaytariladi.
 */
object ErrorMapper {
    fun map(error: String, strings: Language): String {
        return when {
            error.contains("DISTRICT_SCOPE_VIOLATION") -> strings.districtScopeViolation
            error.contains("DATA_INTEGRITY_VIOLATION") -> strings.dataIntegrityViolation
            error.contains("BOOKING_TIME_ALREADY_TAKEN") -> strings.bookingTimeAlreadyTaken

            // Rate limiting (429) — login, OTP yuborish
            error.contains("RATE_LIMIT_EXCEEDED") -> strings.rateLimitExceeded

            // Huquq cheklovlari
            error.contains("STADIUM_SCOPE_VIOLATION") -> strings.accessDenied
            error.contains("USER_SCOPE_VIOLATION") -> strings.accessDenied
            error.contains("NOTIFICATION_SCOPE_VIOLATION") -> strings.accessDenied
            error.contains("DASHBOARD_ACCESS_DENIED") -> strings.accessDenied
            error.contains("BOOKING_CANCEL_NOT_ALLOWED") -> strings.accessDenied
            error.contains("BOOKING_UPDATE_NOT_ALLOWED") -> strings.accessDenied
            error.contains("DISTRICT_ADMIN_CANNOT_ASSIGN_ADMIN_ROLE") -> strings.accessDenied

            // Bron holati
            error.contains("BOOKING_NOT_PENDING") -> strings.bookingNotPending
            error.contains("BOOKING_ALREADY_CONFIRMED") -> strings.bookingNotPending

            else -> error
        }
    }
}
