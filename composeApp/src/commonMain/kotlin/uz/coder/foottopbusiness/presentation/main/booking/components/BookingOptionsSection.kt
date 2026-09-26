package uz.coder.foottopbusiness.presentation.main.booking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.coder.foottopbusiness.core.localization.Localization

/**
 * Bron yaratishdagi qo'shimcha tanlovlar. Qiymatlar backend enum nomlari
 * bilan bir xil, so'rovga o'zgartirmasdan ketadi.
 */
data class BookingOptions(
    val bookingType: String = ONE_TIME,
    val recurrenceCount: Int = DEFAULT_WEEKS,
    val paymentTiming: String = PAY_AFTER_GAME
) {
    val isRecurring: Boolean get() = bookingType == RECURRING

    /** So'rovga ketadigan qiymat: bir martalik bronda yuborilmaydi. */
    val recurrenceCountOrNull: Int? get() = if (isRecurring) recurrenceCount else null

    companion object {
        const val ONE_TIME = "ONE_TIME"
        const val RECURRING = "RECURRING"
        const val PREPAID = "PREPAID"
        const val PAY_AFTER_GAME = "PAY_AFTER_GAME"
        const val MIN_WEEKS = 2
        const val MAX_WEEKS = 12
        const val DEFAULT_WEEKS = 4
    }
}

@Composable
fun BookingOptionsSection(
    options: BookingOptions,
    onChange: (BookingOptions) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = Localization.current

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(strings.bookingTypeLabel)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !options.isRecurring,
                onClick = { onChange(options.copy(bookingType = BookingOptions.ONE_TIME)) },
                label = { Text(strings.bookingOneTime) }
            )
            FilterChip(
                selected = options.isRecurring,
                onClick = { onChange(options.copy(bookingType = BookingOptions.RECURRING)) },
                label = { Text(strings.bookingRecurring) }
            )
        }

        if (options.isRecurring) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    strings.recurrenceWeeks,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onChange(options.copy(recurrenceCount = options.recurrenceCount - 1)) },
                    enabled = options.recurrenceCount > BookingOptions.MIN_WEEKS
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
                Text(
                    "${options.recurrenceCount} ${strings.weeksShort}",
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onChange(options.copy(recurrenceCount = options.recurrenceCount + 1)) },
                    enabled = options.recurrenceCount < BookingOptions.MAX_WEEKS
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }

        SectionLabel(strings.paymentTimingLabel)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = options.paymentTiming == BookingOptions.PREPAID,
                onClick = { onChange(options.copy(paymentTiming = BookingOptions.PREPAID)) },
                label = { Text(strings.paymentPrepaid) }
            )
            FilterChip(
                selected = options.paymentTiming == BookingOptions.PAY_AFTER_GAME,
                onClick = { onChange(options.copy(paymentTiming = BookingOptions.PAY_AFTER_GAME)) },
                label = { Text(strings.paymentAfterGame) }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
