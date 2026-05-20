package uz.coder.foottopbusiness.core.localization

object ErrorMapper {
    fun map(error: String, strings: Language): String {
        return when {
            error.contains("DISTRICT_SCOPE_VIOLATION") -> strings.districtScopeViolation
            error.contains("DATA_INTEGRITY_VIOLATION") -> strings.dataIntegrityViolation
            error.contains("BOOKING_TIME_ALREADY_TAKEN") -> strings.bookingTimeAlreadyTaken
            else -> error
        }
    }
}
