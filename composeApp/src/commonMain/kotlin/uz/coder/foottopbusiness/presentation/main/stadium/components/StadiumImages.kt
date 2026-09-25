package uz.coder.foottopbusiness.presentation.main.stadium.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.platform.PickedImage

/** Backend limitlari: bitta stadionda 10 ta rasm, bitta rasm 5 MB, bitta so'rov jami 30 MB. */
const val MAX_STADIUM_IMAGES = 10
private const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
private const val MAX_UPLOAD_BYTES = 30 * 1024 * 1024

class ImageLimitResult(
    val accepted: List<PickedImage>,
    /** ErrorMapper tushunadigan kod yoki null. */
    val errorCode: String?,
)

/**
 * Tanlangan rasmlarni backend limitlariga moslab filtrlaydi. Limitdan oshsa server
 * butun so'rovni rad etadi, shuning uchun oldindan kesib tashlaymiz.
 *
 * @param currentCount stadionda (yoki formada) allaqachon bor rasmlar soni
 * @param currentBytes shu so'rovga qo'shilib ketadigan rasmlar hajmi
 */
fun applyImageLimits(currentCount: Int, currentBytes: Int, picked: List<PickedImage>): ImageLimitResult {
    val accepted = mutableListOf<PickedImage>()
    var totalBytes = currentBytes
    var skippedBySize = false

    for (image in picked) {
        if (currentCount + accepted.size >= MAX_STADIUM_IMAGES) break
        if (image.bytes.size > MAX_IMAGE_BYTES || totalBytes + image.bytes.size > MAX_UPLOAD_BYTES) {
            skippedBySize = true
            continue
        }
        accepted += image
        totalBytes += image.bytes.size
    }

    val errorCode = when {
        skippedBySize -> "FILE_TOO_LARGE"
        accepted.size < picked.size -> "TOO_MANY_IMAGES"
        else -> null
    }
    return ImageLimitResult(accepted, errorCode)
}

/** Rasm manbasini tanlash: galereya yoki kamera. */
@Composable
private fun ImageSourceDialog(onDismiss: () -> Unit, onGallery: () -> Unit, onCamera: () -> Unit) {
    val strings = Localization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addPhoto) },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text(strings.chooseFromGallery) },
                    leadingContent = { Icon(Icons.Outlined.PhotoLibrary, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onGallery)
                )
                ListItem(
                    headlineContent = { Text(strings.takePhoto) },
                    leadingContent = { Icon(Icons.Outlined.PhotoCamera, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onCamera)
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

/**
 * Stadion rasmlari: kichik ko'rinishlar, har birida o'chirish tugmasi va qo'shish katagi.
 *
 * @param images Coil modeli (ByteArray yoki URL)
 * @param busyIndex shu rasm ustida amal bajarilmoqda (o'chirilmoqda)
 * @param isUploading yangi rasmlar yuklanmoqda
 */
@Composable
fun StadiumImagesPicker(
    images: List<Any>,
    enabled: Boolean,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemove: (Int) -> Unit,
    busyIndex: Int? = null,
    isUploading: Boolean = false,
) {
    val strings = Localization.current
    val canAddMore = images.size < MAX_STADIUM_IMAGES
    var showSourceChooser by remember { mutableStateOf(false) }
    val onAdd = { showSourceChooser = true }

    if (showSourceChooser) {
        ImageSourceDialog(
            onDismiss = { showSourceChooser = false },
            onGallery = {
                showSourceChooser = false
                onPickFromGallery()
            },
            onCamera = {
                showSourceChooser = false
                onTakePhoto()
            }
        )
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                strings.stadiumPhotos,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${images.size}/$MAX_STADIUM_IMAGES",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))

        if (images.isEmpty() && !isUploading) {
            Button(
                onClick = onAdd,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Outlined.AddAPhoto, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.addPhoto, fontWeight = FontWeight.SemiBold)
            }
            return@Column
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(images) { index, image ->
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (busyIndex == index) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .clickable(enabled = enabled) { onRemove(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            if (isUploading || canAddMore) {
                item {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = enabled && !isUploading, onClick = onAdd),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                Icons.Outlined.AddAPhoto,
                                strings.addPhoto,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
