package uz.coder.foottopbusiness.core.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
import uz.coder.foottopbusiness.core.platform.LocationPermissionLauncher
import uz.coder.foottopbusiness.core.platform.PermissionStatus
import uz.coder.foottopbusiness.core.platform.checkLocationPermissionStatus
import uz.coder.foottopbusiness.core.platform.getCurrentLocation
import kotlin.time.Duration.Companion.milliseconds

/** Joriy joylashuvni aniqlash bosqichi — foydalanuvchiga holatni ko'rsatish uchun. */
private enum class LocateState { Idle, Locating, Denied, Failed, OutOfRegion }

private const val TASHKENT_LAT = 41.311081
private const val TASHKENT_LNG = 69.240562
private const val DEFAULT_ZOOM = 12.0
private const val SELECTED_ZOOM = 16.0

/**
 * O'zbekiston chegaralari (taxminiy). Stadionlar faqat shu yerda bo'ladi, shuning uchun
 * undan tashqaridagi GPS natijasi (emulyatorning standart AQSh joylashuvi, eskirgan kesh)
 * avtomatik tanlanmaydi.
 */
private fun isInUzbekistan(lat: Double, lng: Double) = lat in 37.0..45.8 && lng in 55.8..73.3

private fun Double?.isValidCoordinate() = this != null && this != 0.0

/**
 * Joy tanlash xaritasi. Tanlangan joy — ekran markazidagi pin: foydalanuvchi xaritani
 * suradi yoki kerakli joyga bosadi, markaz tanlanadi.
 *
 * @param enabled false bo'lsa faqat ko'rish rejimi (forma ichidagi kichik xarita):
 *   kamera tanlangan joyga ergashadi, GPS so'ralmaydi.
 */
@Composable
fun MapView(
    modifier: Modifier = Modifier,
    initialLatitude: Double?,
    initialLongitude: Double?,
    enabled: Boolean = true,
    onLocationSelected: (Double, Double) -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentOnLocationSelected by rememberUpdatedState(onLocationSelected)

    val hasSelection = initialLatitude.isValidCoordinate() && initialLongitude.isValidCoordinate()
    // Ekran ochilgandagi holat: tahrirlashda mavjud joyni GPS bilan almashtirib yubormaymiz
    val openedWithSelection = remember { hasSelection }

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locateState by remember { mutableStateOf(LocateState.Idle) }
    var permissionRequest by remember { mutableStateOf(false) }
    // Ruxsat so'rovi "Mening joylashuvim" tugmasidan kelganmi. Tugmadan kelgan
    // bo'lsa tanlovni majburan yangilaymiz, ekran ochilishidan bo'lsa - yo'q.
    var permissionFromFab by remember { mutableStateOf(false) }
    var locateAttempt by remember { mutableIntStateOf(0) }
    // Foydalanuvchi o'zi joy tanladimi. GPS kechikib kelsa uning tanlovini bosib ketmasin.
    var userPicked by remember { mutableStateOf(false) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = if (hasSelection) {
                Position(longitude = initialLongitude!!, latitude = initialLatitude!!)
            } else {
                Position(longitude = TASHKENT_LNG, latitude = TASHKENT_LAT)
            },
            zoom = if (hasSelection) SELECTED_ZOOM else DEFAULT_ZOOM
        )
    )

    // Ko'rish rejimida kamera tanlovga ergashadi. rememberCameraState saveable:
    // xarita ekranidan qaytganda eski kamera tiklanib, yangi joy ko'rinmay qolardi.
    if (!enabled) {
        LaunchedEffect(initialLatitude, initialLongitude) {
            if (hasSelection) {
                cameraState.position = CameraPosition(
                    target = Position(longitude = initialLongitude!!, latitude = initialLatitude!!),
                    zoom = SELECTED_ZOOM
                )
            }
        }
    }

    // Foydalanuvchi xaritani surib to'xtatganda markaz tanlanadi
    if (enabled) {
        LaunchedEffect(cameraState) {
            snapshotFlow { cameraState.isCameraMoving }
                .drop(1)
                .filter { moving -> !moving && cameraState.moveReason == CameraMoveReason.GESTURE }
                .collect {
                    userPicked = true
                    val target = cameraState.position.target
                    currentOnLocationSelected(target.latitude, target.longitude)
                }
        }
    }

    suspend fun moveCameraTo(lat: Double, lng: Double) {
        cameraState.animateTo(
            CameraPosition(target = Position(longitude = lng, latitude = lat), zoom = SELECTED_ZOOM),
            duration = 800.milliseconds
        )
    }

    suspend fun locateAndSelect(forceSelect: Boolean) {
        locateState = LocateState.Locating
        val loc = getCurrentLocation()
        if (loc == null) {
            locateState = LocateState.Failed
            return
        }
        userLocation = loc
        val (lat, lng) = loc
        when {
            forceSelect -> {
                locateState = LocateState.Idle
                currentOnLocationSelected(lat, lng)
                moveCameraTo(lat, lng)
            }
            openedWithSelection || userPicked -> locateState = LocateState.Idle
            !isInUzbekistan(lat, lng) -> locateState = LocateState.OutOfRegion
            else -> {
                locateState = LocateState.Idle
                currentOnLocationSelected(lat, lng)
                moveCameraTo(lat, lng)
            }
        }
    }

    LocationPermissionLauncher(
        trigger = permissionRequest,
        onResult = { status ->
            permissionRequest = false
            if (status == PermissionStatus.GRANTED) {
                if (permissionFromFab) {
                    permissionFromFab = false
                    scope.launch { locateAndSelect(forceSelect = true) }
                } else {
                    locateAttempt++
                }
            } else {
                permissionFromFab = false
                locateState = LocateState.Denied
            }
        }
    )

    // Xarita ochilishi bilan joriy joylashuvni aniqlab, uni tanlab qo'yamiz.
    // MapSelectionScreen alohida ekran bo'lgani uchun ruxsat shu yerda so'raladi.
    LaunchedEffect(locateAttempt, enabled) {
        if (!enabled) return@LaunchedEffect
        if (checkLocationPermissionStatus() != PermissionStatus.GRANTED) {
            if (locateAttempt == 0) permissionRequest = true else locateState = LocateState.Denied
            return@LaunchedEffect
        }
        locateAndSelect(forceSelect = false)
    }

    Box(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            cameraState = cameraState,
            onMapClick = { position, _ ->
                if (enabled) {
                    userPicked = true
                    currentOnLocationSelected(position.latitude, position.longitude)
                    scope.launch { moveCameraTo(position.latitude, position.longitude) }
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            }
        )

        // Foydalanuvchining GPS joylashuvi. cameraState.position o'qilishi shart:
        // aks holda xarita surilganda marker qayta joylashmay, joyida qotib qoladi.
        userLocation?.let { (uLat, uLng) ->
            @Suppress("UNUSED_VARIABLE")
            val cameraPosition = cameraState.position
            val userOffset = cameraState.projection
                ?.screenLocationFromPosition(Position(longitude = uLng, latitude = uLat))
            if (userOffset != null) {
                Icon(
                    imageVector = Icons.Default.PersonPinCircle,
                    contentDescription = "Siz shu yerdamisiz",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .absoluteOffset(x = userOffset.x - 18.dp, y = userOffset.y - 18.dp)
                )
            }
        }

        // Tanlangan joy — markazdagi pin. Tanlanishi mumkin bo'lgan joy ham shu (tahrirlash
        // rejimida), shuning uchun u doim ko'rinadi; ko'rish rejimida faqat tanlov bo'lsa.
        if (enabled || hasSelection) {
            CenterPin(
                lifted = enabled && cameraState.isCameraMoving,
                showLabel = hasSelection,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // My Location FAB (Bottom Right)
        if (enabled) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        if (checkLocationPermissionStatus() != PermissionStatus.GRANTED) {
                            permissionFromFab = true
                            permissionRequest = true
                            return@launch
                        }
                        locateAndSelect(forceSelect = true)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mening joylashuvim", modifier = Modifier.size(20.dp))
            }
        }

        // Joylashuvni aniqlash holati
        if (enabled && locateState != LocateState.Idle) {
            LocateStatusChip(
                state = locateState,
                onAction = {
                    if (locateState == LocateState.Denied) {
                        permissionFromFab = true
                        permissionRequest = true
                    } else {
                        scope.launch { locateAndSelect(forceSelect = true) }
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp)
            )
        } else if (enabled && !hasSelection) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Text(
                    "Xaritani suring yoki kerakli joyga bosing",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/** Markazdagi pin: uchi aynan xarita markazida. Surish paytida biroz ko'tariladi. */
@Composable
private fun CenterPin(lifted: Boolean, showLabel: Boolean, modifier: Modifier = Modifier) {
    val pinSize = 48.dp
    val lift by animateDpAsState(if (lifted) 10.dp else 0.dp)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Pin uchidagi soya — tanlanadigan nuqta
        Surface(
            modifier = Modifier.size(8.dp),
            color = Color.Black.copy(alpha = 0.35f),
            shape = CircleShape
        ) {}

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Ustun pastki cheti markazga tushishi uchun yarim balandligiga yuqoriga suramiz
            // (yorliq ~24dp). LocationOn uchi ikonka pastidan ~4dp yuqorida.
            modifier = Modifier.offset(y = -(pinSize / 2) - (if (showLabel) 12.dp else 0.dp) - lift + 4.dp)
        ) {
            if (showLabel) {
                Surface(
                    color = Color.Red,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        "SIZ TANLAGAN JOY",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Tanlangan joy",
                tint = Color.Red,
                modifier = Modifier.size(pinSize)
            )
        }
    }
}

@Composable
private fun LocateStatusChip(state: LocateState, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
        ) {
            if (state == LocateState.Locating) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = when (state) {
                    LocateState.Locating -> "Joylashuv aniqlanmoqda..."
                    LocateState.Denied -> "Joylashuvga ruxsat berilmagan"
                    LocateState.OutOfRegion -> "Joylashuvingiz O'zbekistondan tashqarida"
                    else -> "Joylashuv topilmadi"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = if (state == LocateState.Locating) 8.dp else 0.dp)
            )
            if (state != LocateState.Locating) {
                TextButton(onClick = onAction) {
                    Text(
                        text = when (state) {
                            LocateState.Denied -> "Ruxsat berish"
                            LocateState.OutOfRegion -> "Baribir tanlash"
                            else -> "Qayta urinish"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
