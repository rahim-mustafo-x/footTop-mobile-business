package uz.coder.foottopbusiness.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLanguage = staticCompositionLocalOf<Language> {
    UzLanguage()
}

object Localization {
    val current: Language
        @Composable
        @ReadOnlyComposable
        get() = LocalLanguage.current
}

@Composable
fun ProvideLocalization(
    language: Language = LocalizationManager.getLanguage(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLanguage provides language,
        content = content
    )
}
