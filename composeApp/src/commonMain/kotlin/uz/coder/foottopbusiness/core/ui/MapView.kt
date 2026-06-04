package uz.coder.foottopbusiness.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.Position
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.style.BaseStyle
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onLocationSelected: (Double, Double) -> Unit
) {
    val tashkentLat = 41.311081
    val tashkentLng = 69.240562

    val currentLat = initialLatitude ?: tashkentLat
    val currentLng = initialLongitude ?: tashkentLng

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(longitude = currentLng, latitude = currentLat),
            zoom = 12.0
        )
    )

    // Update camera when initial location changes (e.g. from GPS)
    LaunchedEffect(initialLatitude, initialLongitude) {
        if (initialLatitude != null && initialLongitude != null) {
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

        // Custom Compose Marker Overlay
        if (initialLatitude != null && initialLongitude != null) {
            val markerPosition = Position(longitude = initialLongitude, latitude = initialLatitude)
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
        }
    }
}
