package uz.coder.foottopbusiness.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy
import uz.coder.foottopbusiness.core.log
import kotlin.coroutines.resume
import kotlin.math.max

@Composable
actual fun rememberImagePicker(
    maxItems: Int,
    onPicked: (List<PickedImage>) -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)

    // PHPickerViewController delegate'ni weak ushlaydi, shuning uchun o'zimiz saqlaymiz
    val delegate = remember {
        PickerDelegate { results ->
            scope.launch {
                val images = results.mapIndexedNotNull { index, result ->
                    val data = result.loadImageData() ?: return@mapIndexedNotNull null
                    withContext(Dispatchers.Default) { data.toPickedImage(index) }
                }
                if (images.isNotEmpty()) currentOnPicked(images)
            }
        }
    }

    return {
        val configuration = PHPickerConfiguration().apply {
            selectionLimit = maxItems.coerceAtLeast(1).toLong()
            filter = PHPickerFilter.imagesFilter
        }
        val picker = PHPickerViewController(configuration = configuration)
        picker.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.topmostPresented()
            ?.presentViewController(picker, animated = true, completion = null)
    }
}

private class PickerDelegate(
    private val onResults: (List<PHPickerResult>) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isNotEmpty()) onResults(results)
    }
}

private fun platform.UIKit.UIViewController.topmostPresented(): platform.UIKit.UIViewController {
    var current = this
    while (true) current = current.presentedViewController ?: return current
}

private suspend fun PHPickerResult.loadImageData(): NSData? = suspendCancellableCoroutine { cont ->
    itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, error ->
        if (error != null) log("ImagePicker", "Rasmni o'qib bo'lmadi: ${error.localizedDescription}")
        cont.resume(data)
    }
}

@Composable
actual fun rememberCameraCapture(
    onCaptured: (PickedImage) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    val currentOnCaptured by rememberUpdatedState(onCaptured)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)

    // UIImagePickerController delegate'ni weak ushlaydi
    val delegate = remember {
        CameraDelegate { image ->
            scope.launch {
                val picked = withContext(Dispatchers.Default) { image.toPickedImage(0) }
                picked?.let(currentOnCaptured)
            }
        }
    }

    return {
        val cameraSource = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        if (!UIImagePickerController.isSourceTypeAvailable(cameraSource)) {
            // Simulyatorda kamera yo'q
            currentOnUnavailable()
        } else {
            // Birinchi ochilishda iOS o'zi ruxsat so'raydi (Info.plist: NSCameraUsageDescription)
            val picker = UIImagePickerController().apply {
                sourceType = cameraSource
                this.delegate = delegate
            }
            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?.topmostPresented()
                ?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class CameraDelegate(
    private val onImage: (UIImage) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        (didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)?.let(onImage)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}

private fun NSData.toPickedImage(index: Int): PickedImage? = UIImage(data = this).toPickedImage(index)

/** Rasmni [MAX_IMAGE_SIDE] gacha kichraytirib, orientatsiyani to'g'rilab JPEG'ga siqadi. */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toPickedImage(index: Int): PickedImage? {
    val image = this
    val (width, height) = image.size.useContents { width to height }
    if (width <= 0.0 || height <= 0.0) return null

    val scale = minOf(1.0, MAX_IMAGE_SIDE / max(width, height))
    val targetWidth = width * scale
    val targetHeight = height * scale
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { this.scale = 1.0 }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(targetWidth, targetHeight), format = format)
    val scaled = renderer.imageWithActions { _ ->
        image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
    }

    val jpeg = UIImageJPEGRepresentation(scaled, JPEG_QUALITY) ?: return null
    val length = jpeg.length.toInt()
    if (length == 0) return null
    val bytes = ByteArray(length).apply {
        usePinned { memcpy(it.addressOf(0), jpeg.bytes, jpeg.length) }
    }
    val timestamp = (NSDate().timeIntervalSince1970 * 1000).toLong()
    return PickedImage(bytes = bytes, fileName = "stadium_${timestamp}_$index.jpg")
}

private const val MAX_IMAGE_SIDE = 1920.0
private const val JPEG_QUALITY = 0.85
