package uz.coder.foottopbusiness.core.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.Position
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.style.BaseStyle
import kotlin.time.Duration.Companion.milliseconds
import uz.coder.foottopbusiness.core.platform.LocationPermissionLauncher
import uz.coder.foottopbusiness.core.platform.PermissionStatus
import uz.coder.foottopbusiness.core.platform.checkLocationPermissionStatus
import uz.coder.foottopbusiness.core.platform.getCurrentLocation
import kotlinx.coroutines.launch

/** Joriy joylashuvni aniqlash bosqichi — foydalanuvchiga holatni ko'rsatish uchun. */
private enum class LocateState { Idle, Locating, Denied, Failed }

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    initialLatitude: Double?,
    initialLongitude: Double?,
    enabled: Boolean = true,
    onLocationSelected: (Double, Double) -> Unit
) {
    val tashkentLat = 41.311081
    val tashkentLng = 69.240562
    val scope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locateState by remember { mutableStateOf(LocateState.Idle) }
    var permissionRequest by remember { mutableStateOf(false) }
    // Ruxsat so'rovi "Mening joylashuvim" tugmasidan kelganmi. Tugmadan kelgan
    // bo'lsa tanlovni majburan yangilaymiz, ekran ochilishidan bo'lsa - yo'q.
    var permissionFromFab by remember { mutableStateOf(false) }
    var locateAttempt by remember { mutableIntStateOf(0) }

    // Tahrirlash oqimida allaqachon tanlangan joy bor - uni joriy joylashuv
    // bilan almashtirib yubormaymiz.
    val hasInitialSelection = initialLatitude != null && initialLatitude != 0.0 &&
            initialLongitude != null

    // Use the projection to calculate screen positions of markers
    // This allows them to stay pinned to their geo-coordinates as the map moves

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = if (hasInitialSelection) {
                Position(longitude = initialLongitude, latitude = initialLatitude)
            } else {
                Position(longitude = tashkentLng, latitude = tashkentLat)
            },
            zoom = if (hasInitialSelection) 15.0 else 12.0
        )
    )

    suspend fun locateAndSelect(forceSelect: Boolean) {
        locateState = LocateState.Locating
        val loc = getCurrentLocation()
        if (loc == null) {
            locateState = LocateState.Failed
            return
        }
        userLocation = loc
        locateState = LocateState.Idle
        if (forceSelect || !hasInitialSelection) {
            onLocationSelected(loc.first, loc.second)
            cameraState.animateTo(
                CameraPosition(
                    target = Position(longitude = loc.second, latitude = loc.first),
                    zoom = 15.0
                ),
                duration = 1000.milliseconds
            )
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
    // Ilgari ruxsat shu yerda so'ralmasdi: MapSelectionScreen alohida ekran
    // bo'lgani uchun AddStadium'dagi ruxsat so'rovi bu yerga yetib kelmaydi va
    // ruxsatsiz qurilmada xarita jim turardi.
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
                    onLocationSelected(position.latitude, position.longitude)
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            }
        )

        // User Current GPS Location Marker (Blue)
        userLocation?.let { (uLat, uLng) ->
            val userPos = Position(longitude = uLng, latitude = uLat)
            // projection?.screenLocationFromPosition keeps it pinned to geo-coordinates
            val userOffset = cameraState.projection?.screenLocationFromPosition(userPos) ?: DpOffset.Zero
            
            if (userOffset != DpOffset.Zero) {
                Icon(
                    imageVector = Icons.Default.PersonPinCircle,
                    contentDescription = "Siz shu yerdamisiz",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .absoluteOffset(
                            x = userOffset.x - 18.dp,
                            y = userOffset.y - 18.dp
                        )
                )
            }
        }

        // Selected Location Marker (Red)
        if (initialLatitude != null && initialLongitude != null && initialLatitude != 0.0) {
            val markerPosition = Position(longitude = initialLongitude, latitude = initialLatitude)
            val screenOffset = cameraState.projection?.screenLocationFromPosition(markerPosition) ?: DpOffset.Zero
            
            if (screenOffset != DpOffset.Zero) {
                // Pulsing animation for the selected location
                val infiniteTransition = rememberInfiniteTransition()
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Box(
                    modifier = Modifier.absoluteOffset(
                        x = screenOffset.x - 50.dp,
                        y = screenOffset.y - 75.dp
                    ).width(100.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Pulsing effect centered at the tip of the pin
                    Canvas(modifier = Modifier.size(60.dp).align(Alignment.BottomCenter).offset(y = 30.dp)) {
                        drawCircle(
                            color = Color.Red,
                            radius = (size.minDimension / 2) * pulseScale,
                            alpha = pulseAlpha,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Surface(
                            color = Color.Red,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 6.dp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                "SIZ TANLAGAN JOY",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        
                        Box(contentAlignment = Alignment.Center) {
                            // Shadow/Circle background for the pin
                            Surface(
                                modifier = Modifier.size(24.dp).offset(y = 12.dp),
                                color = Color.Black.copy(alpha = 0.25f),
                                shape = CircleShape
                            ) {}
                            
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Tanlangan joy",
                                tint = Color.Red,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                }
            }
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

        // Joylashuvni aniqlash holati. Ilgari muvaffaqiyatsizlik jim o'tib ketardi
        // va admin nega joy tanlanmaganini bilmasdi.
        if (enabled && locateState != LocateState.Idle) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    if (locateState == LocateState.Locating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = when (locateState) {
                            LocateState.Locating -> "Joylashuv aniqlanmoqda..."
                            LocateState.Denied -> "Joylashuvga ruxsat berilmagan"
                            else -> "Joylashuv topilmadi"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (locateState != LocateState.Locating) {
                        TextButton(
                            onClick = {
                                if (locateState == LocateState.Denied) {
                                    permissionFromFab = true
                                    permissionRequest = true
                                } else {
                                    scope.launch { locateAndSelect(forceSelect = true) }
                                }
                            }
                        ) {
                            Text(
                                text = if (locateState == LocateState.Denied) "Ruxsat berish" else "Qayta urinish",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
