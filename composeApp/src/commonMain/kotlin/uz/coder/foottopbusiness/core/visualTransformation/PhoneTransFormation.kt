package uz.coder.foottopbusiness.core.visualTransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(9)

        val formatted = buildString {
            append("+998")
            if (digits.isNotEmpty()) {
                append(" (")
                append(digits.take(2))
                if (digits.length >= 2) append(")")
            }
            if (digits.length > 2) {
                append(" ")
                append(digits.substring(2, (5).coerceAtMost(digits.length)))
            }
            if (digits.length > 5) {
                append("-")
                append(digits.substring(5, (7).coerceAtMost(digits.length)))
            }
            if (digits.length > 7) {
                append("-")
                append(digits.substring(7, (9).coerceAtMost(digits.length)))
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return formatted.length.coerceAtMost(6)
                if (offset <= 2) return offset + 6
                if (offset <= 5) return offset + 8
                if (offset <= 7) return offset + 9
                if (offset <= 9) return offset + 10
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 6) return 0
                if (offset <= 8) return offset - 6
                if (offset <= 9) return 2
                if (offset <= 13) return (offset - 8).coerceAtMost(5)
                if (offset <= 14) return 5
                if (offset <= 16) return (offset - 9).coerceAtMost(7)
                if (offset <= 17) return 7
                if (offset <= 19) return (offset - 10).coerceAtMost(9)
                return 9
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

fun formatPhoneNumber(phone: String?): String {
    if (phone.isNullOrBlank()) return "+998 (__) ___-__-__"
    val digits = phone.filter { it.isDigit() }
    val cleanDigits = if (digits.length == 12 && digits.startsWith("998")) {
        digits.substring(3)
    } else if (digits.length == 9) {
        digits
    } else {
        return phone
    }
    
    return try {
        "+998 (${cleanDigits.substring(0, 2)}) ${cleanDigits.substring(2, 5)}-${cleanDigits.substring(5, 7)}-${cleanDigits.substring(7, 9)}"
    } catch (e: Exception) {
        phone
    }
}
