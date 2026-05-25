package uz.coder.foottopbusiness.core.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun Base64Image(
    base64: String,
    modifier: Modifier
) {

    val bitmap = remember(base64) {
        try {
            val pure = base64.substringAfter(",")

            val bytes = Base64.decode(
                pure,
                Base64.DEFAULT
            )

            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )?.asImageBitmap()

        } catch (_: Exception) {
            null
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}