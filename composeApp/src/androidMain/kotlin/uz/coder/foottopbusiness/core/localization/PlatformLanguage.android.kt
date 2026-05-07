package uz.coder.foottopbusiness.core.localization

import java.util.Locale

actual fun getSystemLanguage(): String {
    return Locale.getDefault().language
}
