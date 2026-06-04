package uz.coder.foottopbusiness.presentation.main.stadium.edit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.MapView

@Composable
fun LocationPicker(
    latitude: Double?,
    longitude: Double?,
    address: String,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSelectOnMap: () -> Unit,
    onGetCurrentLocation: () -> Unit
) {
    val strings = Localization.current
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            strings.locationInfo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text(strings.preciseAddress) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(strings.addressPlaceholder) }
        )
        
        if (latitude != null && longitude != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.GpsFixed,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "GPS: ${latitude.toString().take(10)}, ${longitude.toString().take(10)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onGetCurrentLocation,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.GpsFixed, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("GPS", fontSize = 12.sp)
            }
            
            Button(
                onClick = onSelectOnMap,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.location, fontSize = 12.sp)
            }
        }
        
        // Real Map Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onSelectOnMap() },
                contentAlignment = Alignment.Center
            ) {
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    initialLatitude = latitude,
                    initialLongitude = longitude,
                    onLocationSelected = { lat, lng ->
                        onLatitudeChange(lat.toString())
                        onLongitudeChange(lng.toString())
                    }
                )
                
                // Overlay to indicate it's clickable and show status
                if (latitude == null || longitude == null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Joylashuvni tanlash uchun bosing",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
