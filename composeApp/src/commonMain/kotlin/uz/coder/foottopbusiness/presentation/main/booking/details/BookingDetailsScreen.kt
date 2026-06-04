package uz.coder.foottopbusiness.presentation.main.booking.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.visualTransformation.formatPhoneNumber
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

class BookingDetailsScreen(private val booking: BookingResponseDto) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollState = rememberScrollState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bron tafsilotlari") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Card
                StatusCard(booking.status)

                // Customer Info
                InfoSection(title = "Mijoz ma'lumotlari", icon = Icons.Default.Person) {
                    DetailRow(Icons.Default.Person, "Ism", booking.name ?: "Noma'lum")
                    if (!booking.phone.isNullOrBlank()) {
                        DetailRow(Icons.Default.Phone, "Telefon", formatPhoneNumber(booking.phone))
                    }
                }

                // Booking Info
                InfoSection(title = "Bron ma'lumotlari", icon = Icons.Default.Event) {
                    val start = booking.startTime.toLocalDateTimeSafe()
                    DetailRow(Icons.Default.CalendarToday, "Sana", start?.date?.toString() ?: "—")
                    DetailRow(Icons.Default.AccessTime, "Vaqt", "${booking.startTime.formatToTime()} - ${booking.endTime.formatToTime()}")
                    DetailRow(Icons.Default.Stadium, "Stadion ID", booking.stadiumId?.toString() ?: "—")
                }

                // Payment Info
                InfoSection(title = "To'lov ma'lumotlari", icon = Icons.Default.Payments) {
                    DetailRow(Icons.Default.Payments, "Umumiy narx", "${booking.totalPrice?.toInt() ?: 0} so'm")
                    DetailRow(Icons.Default.Info, "Status", booking.status ?: "—")
                }
            }
        }
    }

    @Composable
    private fun StatusCard(status: String?) {
        val (color, label) = when (status) {
            "CONFIRMED" -> Color(0xFF4CAF50) to "Tasdiqlangan"
            "PENDING" -> Color(0xFFFFA000) to "Kutilmoqda"
            "CANCELLED" -> Color(0xFFF44336) to "Bekor qilingan"
            else -> MaterialTheme.colorScheme.onSurfaceVariant to (status ?: "Noma'lum")
        }

        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = color)
                Spacer(Modifier.width(12.dp))
                Text(label, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
            }
        }
    }

    @Composable
    private fun InfoSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }

    @Composable
    private fun DetailRow(icon: ImageVector, label: String, value: String) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text("$label:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}
