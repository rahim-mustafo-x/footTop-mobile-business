package uz.coder.foottopbusiness.presentation.main.home.slots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

// ─── Utilities ──────────────────────────────────────────────────────────────

fun isOverlap(s1: LocalDateTime, e1: LocalDateTime, s2: LocalDateTime, e2: LocalDateTime): Boolean {
    return s1 < e2 && e1 > s2
}

fun LocalDateTime.plusMinutes(minutes: Int): LocalDateTime {
    val tz = TimeZone.currentSystemDefault()
    return this.toInstant(tz).plus(minutes, DateTimeUnit.MINUTE).toLocalDateTime(tz)
}

fun durationMinutes(key: String): Int = when(key) {
    "SIXTY" -> 60
    "NINETY" -> 90
    "ONE_HUNDRED_TWENTY" -> 120
    else -> 60
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotsControlScreen(stadium: StadiumResponse, state: HomeContract.State, viewModel: HomeViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    
    val next5Days = remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        (0..4).map { now.plus(it, DateTimeUnit.DAY).toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stadium.name ?: "Slotlar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.handleEvent(HomeContract.Event.ClearStadiumForSlots)
                        navigator.pop()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            // Booking action is intentionally disabled for available slots list.
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Duration Selection
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DurationChip("60 min", state.selectedDuration == "SIXTY") { viewModel.handleEvent(HomeContract.Event.ChangeDuration("SIXTY")) }
                DurationChip("90 min", state.selectedDuration == "NINETY") { viewModel.handleEvent(HomeContract.Event.ChangeDuration("NINETY")) }
                DurationChip("120 min", state.selectedDuration == "ONE_HUNDRED_TWENTY") { viewModel.handleEvent(HomeContract.Event.ChangeDuration("ONE_HUNDRED_TWENTY")) }
            }

            // Days Selection
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(next5Days) { dateStr ->
                    val isSelected = state.selectedDate == dateStr
                    val dateParts = dateStr.split("-")
                    val day = dateParts.last()
                    
                    Column(
                        modifier = Modifier
                            .width(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.handleEvent(HomeContract.Event.ChangeDate(dateStr)) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        val label = when(dateStr) {
                            next5Days[0] -> "Bugun"
                            next5Days[1] -> "Ertaga"
                            else -> ""
                        }
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Available Slots", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                
            state.stadiumSlots.firstOrNull { it.third }?.let { (start, _, _) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("Earliest available: ", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(start.formatAsTime(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            if (state.isLoadingSlots) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) 
                }
            } else if (state.stadiumSlots.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.EventNote, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("Ushbu kunga ma'lumot topilmadi", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val durationMins = durationMinutes(state.selectedDuration)
                val selectedStart = state.selectedSlot?.first
                val selectedEnd = selectedStart?.plusMinutes(durationMins)
                val strings = Localization.current

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.stadiumSlots.size) { index ->
                        val slot = state.stadiumSlots[index]
                        val (start, end, available) = slot
                        
                        val isPartiallySelected = selectedStart != null && selectedEnd != null &&
                                                 isOverlap(selectedStart, selectedEnd, start, end)
                        
                        val successColor = MaterialTheme.colorScheme.primary
                        val errorColor = Color(0xFFEF5350)
                        
                        val baseColor = if (!available) errorColor else if (isPartiallySelected) MaterialTheme.colorScheme.onPrimary else successColor
                        val bgColor = if (!available) errorColor.copy(alpha = 0.1f) else if (isPartiallySelected) successColor else successColor.copy(alpha = 0.1f)
                        val borderColor = if (isPartiallySelected) successColor else baseColor.copy(alpha = 0.3f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                                .clickable(enabled = available) {
                                    viewModel.handleEvent(HomeContract.Event.SelectSlot(slot))
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPartiallySelected) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = baseColor.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(12.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(start.formatAsTime(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = baseColor)
                                Text(if (available) "Bo'sh" else "Band", fontSize = 12.sp, color = baseColor.copy(alpha = 0.8f))
                            }
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("Bo'sh", MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("Band", MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("Sig'maydi", MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("O'tib ketgan", MaterialTheme.colorScheme.outline)
                }
            }
        }

        if (state.isBookingSlot && state.selectedSlot != null) {
            val durationMins = durationMinutes(state.selectedDuration)
            val selectedStart = state.selectedSlot.first
            val selectedEnd = selectedStart.plusMinutes(durationMins)
            
            var fullName by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            val strings = Localization.current
            val durationText = when(state.selectedDuration) {
                "SIXTY" -> "60 min"
                "NINETY" -> "90 min"
                "ONE_HUNDRED_TWENTY" -> "120 min"
                else -> ""
            }

            val startTimeStr = selectedStart.formatAsTime()
            val endTimeStr = selectedEnd.formatAsTime()

            AlertDialog(
                onDismissRequest = { viewModel.handleEvent(HomeContract.Event.DismissBookingDialog) },
                title = { Text("${strings.bookNow}: $startTimeStr - $endTimeStr") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${strings.duration}: $durationText",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(strings.fullName) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(strings.phoneNumber) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.handleEvent(HomeContract.Event.CreateBooking(fullName, phone)) },
                        enabled = fullName.isNotBlank() && phone.length >= 9
                    ) {
                        Text(strings.save)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.handleEvent(HomeContract.Event.DismissBookingDialog) }) {
                        Text(strings.cancel)
                    }
                }
            )
        }
    }
}

@Composable
fun DurationChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
