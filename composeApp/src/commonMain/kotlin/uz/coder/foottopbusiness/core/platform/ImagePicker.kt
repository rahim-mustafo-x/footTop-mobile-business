package uz.coder.foottopbusiness.core.platform

import androidx.compose.runtime.Composable

/**
 * Galereyadan tanlangan, serverga yuborishga tayyor rasm.
 * Platforma tomonida JPEG'ga siqilgan (backend limiti: bitta rasm 5 MB, jami 30 MB).
 */
class PickedImage(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String = "image/jpeg",
)

/**
 * Galereyadan bir nechta rasm tanlash oynasini ochuvchi funksiyani qaytaradi.
 * Foydalanuvchi bekor qilsa [onPicked] chaqirilmaydi.
 *
 * @param maxItems bir martada tanlash mumkin bo'lgan rasmlar soni (kamida 1).
 */
@Composable
expect fun rememberImagePicker(
    maxItems: Int,
    onPicked: (List<PickedImage>) -> Unit,
): () -> Unit

/**
 * Kamerani ochib bitta surat olish funksiyasini qaytaradi. Rasm [rememberImagePicker]
 * dagidek siqiladi. Bekor qilinsa hech narsa chaqirilmaydi, qurilmada kamera
 * bo'lmasa [onUnavailable].
 */
@Composable
expect fun rememberCameraCapture(
    onCaptured: (PickedImage) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit
