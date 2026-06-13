package uz.coder.foottopbusiness.core.ui

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
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
import uz.coder.foottopbusiness.core.platform.getCurrentLocation
import kotlinx.coroutines.launch

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onLocationSelected: (Double, Double) -> Unit
) {
    val tashkentLat = 41.311081
    val tashkentLng = 69.240562
    val scope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // If initial is 0.0, treat as null
    val safeLat = if (initialLatitude == 0.0) null else initialLatitude
    val safeLng = if (initialLongitude == 0.0) null else initialLongitude

    val currentLat = safeLat ?: userLocation?.first ?: tashkentLat
    val currentLng = safeLng ?: userLocation?.second ?: tashkentLng

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(longitude = currentLng, latitude = currentLat),
            zoom = if (safeLat != null) 15.0 else 12.0
        )
    )

    // Automatically get user location on entry if no location is provided
    LaunchedEffect(Unit) {
        val loc = getCurrentLocation()
        loc?.let {
            userLocation = it
            if (safeLat == null || safeLng == null) {
                cameraState.animateTo(
                    CameraPosition(
                        target = Position(longitude = it.second, latitude = it.first),
                        zoom = 15.0
                    ),
                    duration = 1000.milliseconds
                )
                onLocationSelected(it.first, it.second)
            }
        }
    }

    // Update camera when initial location changes (e.g. from external source)
    LaunchedEffect(initialLatitude, initialLongitude) {
        if (initialLatitude != null && initialLongitude != null && initialLatitude != 0.0 && initialLongitude != 0.0) {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(longitude = initialLongitude, latitude = initialLatitude),
                    zoom = 15.0
                ),
                duration = 1000.milliseconds
            )
        }
    }

    Box(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            cameraState = cameraState,
            onMapClick = { position, _ ->
                onLocationSelected(position.latitude, position.longitude)
                ClickResult.Consume
            }
        )

        // "You are here" Marker (User Location)
        userLocation?.let { (uLat, uLng) ->
            val userPos = Position(longitude = uLng, latitude = uLat)
            val userOffset = cameraState.projection?.screenLocationFromPosition(userPos) ?: DpOffset.Zero
            if (userOffset != DpOffset.Zero) {
                Icon(
                    imageVector = Icons.Default.PersonPinCircle,
                    contentDescription = "Siz shu yerdamisiz",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .absoluteOffset(
                            x = userOffset.x - 16.dp,
                            y = userOffset.y - 16.dp
                        )
                )
            }
        }

        // Selection Marker (Where the stadium is)
        if (safeLat != null && safeLng != null) {
            val markerPosition = Position(longitude = safeLng, latitude = safeLat)
            val screenOffset = cameraState.projection?.screenLocationFromPosition(markerPosition) ?: DpOffset.Zero
            
            if (screenOffset != DpOffset.Zero) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .size(40.dp)
                        .absoluteOffset(
                            x = screenOffset.x - 20.dp,
                            y = screenOffset.y - 40.dp
                        )
                )
            }
        } else {
            // Location not found overlay
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Joylashuv topilmadi. Tanlash uchun xaritaga bosing.",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // My Location FAB
        SmallFloatingActionButton(
            onClick = {
                scope.launch {
                    val loc = getCurrentLocation()
                    loc?.let {
                        userLocation = it
                        cameraState.animateTo(
                            CameraPosition(
                                target = Position(longitude = it.second, latitude = it.first),
                                zoom = 15.0
                            ),
                            duration = 1000.milliseconds
                        )
                        onLocationSelected(it.first, it.second)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mening joylashuvim", modifier = Modifier.size(20.dp))
        }
    }
}
