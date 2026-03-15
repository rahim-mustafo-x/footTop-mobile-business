package uz.coder.foottopbusiness.core.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun UniversalClickableText(
    textParts: List<Pair<String, String?>> = emptyList(), // Pair<matn, tag> null bo‘lsa oddiy
    styles: Map<String, SpanStyle> = emptyMap(),          // tag bo‘yicha style
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    onClick: (tag: String) -> Unit
) {
    val annotatedText = remember(textParts) {
        buildAnnotatedString {
            textParts.forEach { (partText, tag) ->
                if (tag != null) pushStringAnnotation(tag = tag, annotation = tag)
                withStyle(styles[tag] ?: SpanStyle(fontSize = 14.sp)) {
                    append(partText)
                }
                if (tag != null) pop()
            }
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedText,
        style = TextStyle(fontSize = 14.sp, textAlign = textAlign),
        modifier = modifier
            .pointerInput(annotatedText) {
                detectTapGestures { offset: Offset ->
                    textLayoutResult?.let { layoutResult ->
                        val position = layoutResult.getOffsetForPosition(offset)
                        annotatedText.getStringAnnotations(
                            start = position,
                            end = position
                        ).firstOrNull()?.let { annotation ->
                            onClick(annotation.tag)
                        }
                    }
                }
            },
        onTextLayout = { textLayoutResult = it }
    )
}