package uz.coder.foottopbusiness.core.visualTransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class AmountTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedText = StringBuilder()
        for (i in originalText.indices) {
            formattedText.append(originalText[i])
            val remaining = originalText.length - 1 - i
            if (remaining > 0 && remaining % 3 == 0) {
                formattedText.append(" ")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val len = originalText.length
                var transformedOffset = offset
                for (i in 1..offset) {
                    val remainingFromRight = len - i
                    if (remainingFromRight > 0 && remainingFromRight % 3 == 0) {
                        transformedOffset++
                    }
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                val transformedStr = formattedText.toString()
                var spaces = 0
                for (i in 0 until offset.coerceAtMost(transformedStr.length)) {
                    if (transformedStr[i] == ' ') {
                        spaces++
                    }
                }
                return (offset - spaces).coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText.toString()), offsetMapping)
    }
}
