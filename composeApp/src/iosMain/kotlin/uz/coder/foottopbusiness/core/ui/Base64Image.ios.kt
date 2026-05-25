package uz.coder.foottopbusiness.core.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.dataWithData
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import org.jetbrains.skia.Image

@OptIn(BetaInteropApi::class)
@Composable
actual fun Base64Image(
    base64: String,
    modifier: Modifier
) {

    val imageBitmap = remember(base64) {

        try {

            val pure = base64.substringAfter(",")

            val data = NSData.create(
                base64EncodedString = pure,
                options = 0u
            )

            val uiImage = data?.let {
                UIImage(data = it)
            }

            uiImage
                ?.toImageBitmap()

        } catch (_: Exception) {
            null
        }
    }

    imageBitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toImageBitmap(): ImageBitmap {

    val data = NSData.dataWithData(
        UIImagePNGRepresentation(this)!!
    )

    val bytes = ByteArray(data.length.toInt())

    for (i in bytes.indices) {
        bytes[i] = data.bytes!!.readBytes(data.length.toInt())[i]
    }

    return Image.makeFromEncoded(bytes)
        .toComposeImageBitmap()
}