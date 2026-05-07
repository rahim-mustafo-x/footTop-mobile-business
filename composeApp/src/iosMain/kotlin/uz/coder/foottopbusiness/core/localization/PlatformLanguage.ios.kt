package uz.coder.foottopbusiness.core.localization

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun getSystemLanguage(): String {
    return NSLocale.currentLocale.languageCode ?: "uz"
}
