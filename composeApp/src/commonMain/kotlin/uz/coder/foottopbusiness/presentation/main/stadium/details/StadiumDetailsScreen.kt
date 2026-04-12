package uz.coder.foottopbusiness.presentation.main.stadium.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.main.stadium.edit.EditStadiumVoyager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StadiumDetailsScreen(viewModel: StadiumDetailsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stadium = state.stadium
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                StadiumDetailsContract.Effect.NavigateBack -> onBack()
                is StadiumDetailsContract.Effect.NavigateToEdit -> {
                    navigator.push(EditStadiumVoyager(effect.stadium))
                }
                is StadiumDetailsContract.Effect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stadium?.name ?: "Stadium Details", color = Primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.BackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    if (stadium != null) {
                        IconButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.EditClick) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Primary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (stadium == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Header
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=1000",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stadium.name ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (stadium.isActive == true) "Faol" else "Nofaol",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (stadium.isActive == true) Color(0xFF4CAF50) else Color.Gray
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = stadium.isActive == true,
                                onCheckedChange = { viewModel.handleEvent(StadiumDetailsContract.Event.ToggleActive(it)) },
                                enabled = !state.isUpdatingStatus,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    DetailItem(Icons.Default.LocationOn, "Manzil", "${stadium.regionName}, ${stadium.districtName}")
                    DetailItem(Icons.Default.Stadium, "Tur", stadium.type ?: "Football")
                    DetailItem(Icons.Default.Stadium, "Sig'im", "${stadium.capacity ?: 0} kishi")
                    DetailItem(Icons.Default.Stadium, "Narx", "${stadium.pricePerHour?.toInt() ?: 0} so'm/soat")
                    DetailItem(Icons.Default.Stadium, "Ish vaqti", "${
                        stadium.openTime?.let {
                            try { LocalDateTime.parse(it).formatAsTime() } catch (_: Exception) { it.take(5) }
                        } ?: ""} - ${
                        stadium.closeTime?.let { 
                            try { LocalDateTime.parse(it).formatAsTime() } catch (_: Exception) { it.take(5) }
                        } ?: ""}")

                    Spacer(Modifier.height(24.dp))
                    Text("Bo'sh vaqtlar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Spacer(Modifier.height(12.dp))

                    if (stadium.slots.isNullOrEmpty()) {
                        Text("Ma'lumot mavjud emas", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(80.dp),
                            modifier = Modifier.heightIn(max = 400.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(stadium.slots) { slot ->
                                val isAvailable = slot.status == "AVAILABLE"
                                val isBooked = slot.status == "BOOKED"
                                val isPast = slot.status == "PAST"
                                val color = when {
                                    isAvailable -> Color(0xFF4CAF50)
                                    isBooked -> Color(0xFFF44336)
                                    isPast -> Color.Gray
                                    else -> Color.DarkGray
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = color.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.handleEvent(StadiumDetailsContract.Event.SlotClick(slot)) }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = slot.start?.take(5) ?: "",
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Tavsif", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stadium.description ?: "Tavsif mavjud emas.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(Modifier.height(32.dp))

                    // Add Pitch Button
                    Button(
                        onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.ShowAddPitchDialog) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pitch qo'shish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.EditClick) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Stadionni tahrirlash", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Add Pitch Dialog
    if (state.showAddPitchDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissAddPitchDialog) },
            title = { Text("Pitch qo'shish", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.pitchName,
                        onValueChange = { viewModel.handleEvent(StadiumDetailsContract.Event.PitchNameChanged(it)) },
                        label = { Text("Pitch nomi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.pitchStartTime,
                        onValueChange = { viewModel.handleEvent(StadiumDetailsContract.Event.PitchStartTimeChanged(it)) },
                        label = { Text("Boshlanish vaqti (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("09:00") }
                    )
                    OutlinedTextField(
                        value = state.pitchEndTime,
                        onValueChange = { viewModel.handleEvent(StadiumDetailsContract.Event.PitchEndTimeChanged(it)) },
                        label = { Text("Tugash vaqti (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("18:00") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.SavePitch) }) {
                    Text("Saqlash", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissAddPitchDialog) }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // Slot Action Dialog
    if (state.showSlotActionDialog && state.selectedSlot != null) {
        val slot = state.selectedSlot!!
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissSlotDialog) },
            title = { Text("Vaqt: ${slot.start?.take(5)} - ${slot.end?.take(5)}", color = Primary) },
            text = {
                Column {
                    Text("Holati: ${slot.status ?: "Noma'lum"}")
                    Spacer(Modifier.height(8.dp))
                    Text("Ushbu vaqtni band qilmoqchimisiz yoki holatini o'zgartirmoqchimisiz?")
                }
            },
            confirmButton = {
                if (slot.status == "AVAILABLE") {
                    Button(
                        onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.BookSlot) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Band qilish")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissSlotDialog) }) {
                    Text("Yopish")
                }
            }
        )
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
