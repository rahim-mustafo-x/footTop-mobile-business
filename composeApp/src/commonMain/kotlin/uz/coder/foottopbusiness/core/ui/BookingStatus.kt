package uz.coder.foottopbusiness.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import uz.coder.foottopbusiness.core.localization.Language

/** Bron holatining rangi va foydalanuvchi tilidagi nomi (ro'yxat, tafsilot, jadval). */
@Composable
fun bookingStatusStyle(status: String?, strings: Language): Pair<Color, String> = when (status) {
    "CONFIRMED" -> Success to strings.statusConfirmed
    "PENDING" -> Warning to strings.statusPending
    "CANCELLED" -> Error to strings.statusCancelled
    "REJECTED" -> Error to strings.statusRejected
    "COMPLETED" -> MaterialTheme.colorScheme.primary to strings.statusCompleted
    else -> MaterialTheme.colorScheme.onSurfaceVariant to (status ?: strings.unknown)
}
