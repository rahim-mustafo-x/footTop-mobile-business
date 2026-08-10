package uz.coder.foottopbusiness.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/**
 * Ilova tipografiyasi.
 *
 * M3 standart o'lchamlari, lineHeight va letterSpacing qiymatlari o'zgarmagan -
 * faqat fontWeight ilovaning "qalin sarlavha" uslubiga moslashtirilgan.
 * Shu sababli Material komponentlari (TopAppBar, Button, AlertDialog, Snackbar)
 * o'lchamini o'zgartirmasdan, umumiy ko'rinishga mos keladi.
 */
private val Default = Typography()

val AppTypography = Typography(
    displayLarge = Default.displayLarge.copy(fontWeight = FontWeight.Black),
    displayMedium = Default.displayMedium.copy(fontWeight = FontWeight.Black),
    displaySmall = Default.displaySmall.copy(fontWeight = FontWeight.Black),

    headlineLarge = Default.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
    headlineMedium = Default.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = Default.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),

    titleLarge = Default.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = Default.titleMedium.copy(fontWeight = FontWeight.Bold),
    titleSmall = Default.titleSmall.copy(fontWeight = FontWeight.SemiBold),

    // Asosiy matn qalinligi o'zgarmaydi - o'qilishi uchun Normal qoladi
    bodyLarge = Default.bodyLarge,
    bodyMedium = Default.bodyMedium,
    bodySmall = Default.bodySmall,

    labelLarge = Default.labelLarge.copy(fontWeight = FontWeight.Bold),
    labelMedium = Default.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = Default.labelSmall.copy(fontWeight = FontWeight.SemiBold),
)
