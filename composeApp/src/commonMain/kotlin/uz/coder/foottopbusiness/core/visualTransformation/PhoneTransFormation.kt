package uz.coder.foottopbusiness.core.visualTransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(9)

        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                append(c)
                if (index == 1 || index == 4 || index == 6) append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, digits.length)
                var extra = 0
                if (clamped > 2) extra++ // space after index 1
                if (clamped > 5) extra++ // space after index 4
                if (clamped > 7) extra++ // space after index 6
                return (clamped + extra).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return formatted.take(clamped).count { it.isDigit() }.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}