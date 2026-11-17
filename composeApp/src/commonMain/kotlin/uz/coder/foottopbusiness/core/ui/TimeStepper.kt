package uz.coder.foottopbusiness.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val DAY = 24 * 60

/** "HH:mm" vaqtini [step] daqiqalik qadam bilan o'zgartiradi (yarim tundan aylanadi). */
@Composable
fun TimeStepper(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    step: Int = 30,
    onChange: (String) -> Unit
) {
    val minutes = value.substringBefore(':').toIntOrNull()?.times(60)?.plus(value.substringAfter(':', "0").toIntOrNull() ?: 0) ?: 0
    val shift = { delta: Int ->
        val m = ((minutes + delta) % DAY + DAY) % DAY
        onChange("${(m / 60).toString().padStart(2, '0')}:${(m % 60).toString().padStart(2, '0')}")
    }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { shift(-step) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, null, Modifier.size(18.dp))
                }
                Text(value, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                IconButton(onClick = { shift(step) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                }
            }
        }
    }
}
