package uz.coder.foottopbusiness.presentation.main.stadium.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.TimeStepper
import uz.coder.foottopbusiness.core.visualTransformation.AmountTransformation
import uz.coder.foottopbusiness.data.network.dto.stadium.PeakPricing
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

/** Formadagi gavjum narx holati. Bo'sh narx - kun bo'yi bitta narx. */
data class PeakForm(
    val price: String = "",
    val from: String = "18:00",
    val to: String = "23:00",
    val weekends: Boolean = false
) {
    val priceValue: Int get() = price.filter { it.isDigit() }.toIntOrNull() ?: 0

    /** Gavjum narx bor, lekin qachon amal qilishi berilmagan. */
    val needsWindow: Boolean get() = priceValue > 0 && from.isBlank() && !weekends

    /** So'rovga ketadigan qiymat: 0 gavjum narxni o'chiradi. */
    fun toRequest(): PeakPricing = if (priceValue > 0) {
        PeakPricing(priceValue, from.ifBlank { null }, to.ifBlank { null }, weekends)
    } else {
        PeakPricing(0, null, null, false)
    }

    companion object {
        fun of(stadium: StadiumResponse?, closeTime: String): PeakForm {
            val price = stadium?.peakPricePerHour?.toLong()?.takeIf { it > 0 }
            return PeakForm(
                price = price?.toString().orEmpty(),
                from = stadium?.peakStartTime?.take(5)?.takeIf { it.isNotBlank() } ?: "18:00",
                to = stadium?.peakEndTime?.take(5)?.takeIf { it.isNotBlank() } ?: closeTime,
                weekends = stadium?.peakWeekends == true
            )
        }
    }
}

@Composable
fun PeakPricingSection(form: PeakForm, onChange: (PeakForm) -> Unit) {
    val strings = Localization.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(strings.peakPricingHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = form.price,
            onValueChange = { onChange(form.copy(price = it.filter(Char::isDigit).take(9))) },
            label = { Text("${strings.peakPrice} (${strings.optional})") },
            placeholder = { Text("150 000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = AmountTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        if (form.priceValue > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeStepper(strings.timeFrom, form.from, Modifier.weight(1f)) { onChange(form.copy(from = it)) }
                TimeStepper(strings.timeTo, form.to, Modifier.weight(1f)) { onChange(form.copy(to = it)) }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.peakWeekends, Modifier.weight(1f))
                Switch(checked = form.weekends, onCheckedChange = { onChange(form.copy(weekends = it)) })
            }
        }
    }
}
