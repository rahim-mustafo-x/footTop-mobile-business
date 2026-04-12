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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

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
                DurationChip("120 min", state.selectedDuration == "HUNDRED_TWENTY") { viewModel.handleEvent(HomeContract.Event.ChangeDuration("HUNDRED_TWENTY")) }
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
                            .background(if (isSelected) Primary else MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.handleEvent(HomeContract.Event.ChangeDate(dateStr)) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
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
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Available Slots", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                
                state.stadiumSlots.firstOrNull { it.third }?.let { earliest ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp), tint = Color(0xFF388E3C))
                        Spacer(Modifier.width(6.dp))
                        Text("Earliest available: ", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(earliest.first.formatAsTime(), color = Color(0xFF388E3C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            if (state.isLoadingSlots) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = Primary) 
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.stadiumSlots) { slot ->
                        val (start, _, available) = slot
                        val isSelected = state.selectedSlot == slot
                        val baseColor = if (!available) Color(0xFFF44336) else if (isSelected) Color.White else Color(0xFF388E3C)
                        val bgColor = if (!available) Color(0xFFF44336).copy(alpha = 0.1f) else if (isSelected) Color(0xFF388E3C) else Color(0xFF388E3C).copy(alpha = 0.1f)
                        val borderColor = if (isSelected) Color(0xFF388E3C) else baseColor.copy(alpha = 0.3f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
                    LegendItem("Bo'sh", Color(0xFF388E3C))
                    Spacer(Modifier.width(16.dp))
                    LegendItem("Band", Color(0xFFF44336))
                    Spacer(Modifier.width(16.dp))
                    LegendItem("Sig'maydi", Color.LightGray)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("O'tib ketgan", Color.LightGray)
                }
            }
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
            .background(if (isSelected) Primary else Color.White)
            .border(1.dp, if (isSelected) Primary else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
