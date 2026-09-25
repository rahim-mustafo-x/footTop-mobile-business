package uz.coder.foottopbusiness.core.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.coder.foottopbusiness.core.log
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

@Composable
actual fun rememberImagePicker(
    maxItems: Int,
    onPicked: (List<PickedImage>) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)

    val handleUris: (List<Uri>) -> Unit = { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val images = withContext(Dispatchers.IO) {
                    uris.mapIndexedNotNull { index, uri -> context.readAsJpeg(uri, index) }
                }
                if (images.isNotEmpty()) currentOnPicked(images)
            }
        }
    }

    // PickMultipleVisualMedia maxItems <= 1 bo'lsa IllegalArgumentException tashlaydi
    val multiLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems.coerceAtLeast(2))
    ) { uris -> handleUris(uris.take(maxItems)) }
    val singleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> handleUris(listOfNotNull(uri)) }

    return {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        if (maxItems > 1) multiLauncher.launch(request) else singleLauncher.launch(request)
    }
}

@Composable
actual fun rememberCameraCapture(
    onCaptured: (PickedImage) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnCaptured by rememberUpdatedState(onCaptured)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    // Kamera ochiqligida Activity qayta yaratilishi mumkin, fayl yo'li yo'qolmasin
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val file = pendingPath?.let(::File) ?: return@rememberLauncherForActivityResult
        pendingPath = null
        if (!saved) {
            file.delete()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val image = withContext(Dispatchers.IO) {
                context.readAsJpeg(Uri.fromFile(file), 0).also { file.delete() }
            }
            image?.let(currentOnCaptured)
        }
    }

    return {
        try {
            val dir = File(context.cacheDir, "camera").apply { mkdirs() }
            val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
            // Manifest'dagi FileProvider (authority = applicationId + ".fileprovider", cache-path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            pendingPath = file.absolutePath
            launcher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingPath = null
            log("ImagePicker", "Kamera ilovasi topilmadi: ${e.message}")
            currentOnUnavailable()
        }
    }
}

/** Rasmni o'qib, [MAX_IMAGE_SIDE] gacha kichraytiradi va JPEG'ga siqadi. */
private fun Context.readAsJpeg(uri: Uri, index: Int): PickedImage? = try {
    val bitmap = decodeScaled(uri)
    val bytes = ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        out.toByteArray()
    }
    bitmap.recycle()
    PickedImage(bytes = bytes, fileName = "stadium_${System.currentTimeMillis()}_$index.jpg")
} catch (e: Exception) {
    log("ImagePicker", "Rasmni o'qib bo'lmadi: $uri, ${e.message}")
    null
}

private fun Context.decodeScaled(uri: Uri): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // ImageDecoder EXIF orientatsiyasini ham hisobga oladi
        val source = ImageDecoder.createSource(contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val largest = max(info.size.width, info.size.height)
            if (largest > MAX_IMAGE_SIDE) {
                val scale = MAX_IMAGE_SIDE.toFloat() / largest
                decoder.setTargetSize(
                    (info.size.width * scale).toInt().coerceAtLeast(1),
                    (info.size.height * scale).toInt().coerceAtLeast(1),
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sampleSize = 1
    while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_IMAGE_SIDE) sampleSize *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        ?: throw IllegalStateException("Rasmni dekodlab bo'lmadi")

    // BitmapFactory EXIF orientatsiyasini hisobga olmaydi -- kamera suratlari yonboshlab qoladi
    val degrees = try {
        contentResolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
    } catch (_: Exception) {
        0f
    }
    if (degrees == 0f) return bitmap
    val rotated = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true
    )
    if (rotated != bitmap) bitmap.recycle()
    return rotated
}

private const val MAX_IMAGE_SIDE = 1920
private const val JPEG_QUALITY = 85
