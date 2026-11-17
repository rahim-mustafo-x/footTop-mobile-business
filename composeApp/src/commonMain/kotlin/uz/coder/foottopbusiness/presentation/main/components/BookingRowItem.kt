package uz.coder.foottopbusiness.presentation.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.coder.foottopbusiness.core.Money
import uz.coder.foottopbusiness.core.formatAsDate
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.ui.bookingStatusStyle
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

/**
 * Bitta bron - ixcham qator (mijoz tarixi, qarzdorlar ro'yxati).
 * [title] berilmasa stadion nomi chiqadi; [action] o'ng tomondagi tugma.
 */
@Composable
fun BookingRowItem(
    booking: BookingResponseDto,
    onClick: () -> Unit,
    title: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    val strings = Localization.current
    val start = booking.startTime.toLocalDateTimeSafe()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    start?.formatAsDate()?.take(5) ?: "—",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    booking.startTime.formatToTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title ?: booking.stadiumName ?: strings.stadium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val paid = if (booking.paymentStatus == "PAID") " · ${strings.paidLabel}" else ""
                Text(
                    Money.withCurrency(booking.totalPrice ?: 0.0, strings.currency) + paid,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val (color, label) = if (booking.noShow == true) {
                MaterialTheme.colorScheme.onSurfaceVariant to strings.noShow
            } else {
                bookingStatusStyle(booking.status, strings)
            }
            Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    label,
                    color = color,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            if (action != null) {
                Spacer(Modifier.width(2.dp))
                action()
            }
        }
    }
}
