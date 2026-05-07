package uz.coder.foottopbusiness.core.localization

expect fun getSystemLanguage(): String

object LocalizationManager {
    fun getLanguage(): Language {
        return when (getSystemLanguage()) {
            "ru" -> RuLanguage()
            "en" -> EnLanguage()
            "uz" -> UzLanguage()
            else -> UzLanguage() // Default to Uzbek as requested or based on project context
        }
    }
}
