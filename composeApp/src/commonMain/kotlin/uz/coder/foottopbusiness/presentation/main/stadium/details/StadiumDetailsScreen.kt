package uz.coder.foottopbusiness.presentation.main.stadium.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import uz.coder.foottopbusiness.core.formatToTime
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
                title = { Text(stadium?.name ?: "Stadion ma'lumotlari", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.BackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (stadium != null) {
                        IconButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.EditClick) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (stadium == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding(), start = paddingValues.calculateStartPadding(
                        LayoutDirection.Ltr), end = paddingValues.calculateEndPadding(LayoutDirection.Rtl))
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=1000",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay for better text visibility if needed, or just style
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(16.dp)
                ) {
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = if (stadium.isActive == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                            Text(
                                text = if (stadium.isActive == true) "Faol" else "Nofaol",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
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
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    val addressName = if (stadium.regionName!=null && stadium.districtName!=null){
                        "${stadium.regionName}, ${stadium.districtName}"
                    }else{
                        "Aniqlanmagan"
                    }
                    val stadiumType = stadium.type?.uppercase()?.first().toString()+stadium.type?.lowercase()?.substring(1, stadium.type.length)
                    DetailItem(Icons.Default.LocationOn, "Manzil", addressName)
                    DetailItem(Icons.Default.Stadium, "Tur", stadiumType)
                    DetailItem(Icons.Default.Stadium, "Sig'im", "${stadium.capacity ?: 0} kishi")
                    DetailItem(Icons.Default.Stadium, "Narx", "${stadium.pricePerHour?.toInt() ?: 0} so'm/soat")
                    DetailItem(Icons.Default.Stadium, "Ish vaqti", "${stadium.openTime.formatToTime()} - ${stadium.closeTime.formatToTime()}")

                    Spacer(Modifier.height(24.dp))
                    Text("Bo'sh vaqtlar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))

                    if (stadium.slots.isNullOrEmpty()) {
                        Text("Ma'lumot mavjud emas", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    isAvailable -> MaterialTheme.colorScheme.primary
                                    isBooked -> MaterialTheme.colorScheme.error
                                    isPast -> MaterialTheme.colorScheme.outline
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                                        text = slot.start.formatToTime(),
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Tavsif", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pitch qo'shish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.EditClick) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stadionni tahrirlash", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Add Pitch Dialog
    if (state.showAddPitchDialog) {
        var showStartTimePicker by remember { mutableStateOf(false) }
        var showEndTimePicker by remember { mutableStateOf(false) }

        if (showStartTimePicker) {
            val initialHour = try { state.pitchStartTime.split(":")[0].toInt() } catch (_: Exception) { 9 }
            val initialMinute = try { state.pitchStartTime.split(":")[1].toInt() } catch (_: Exception) { 0 }
            uz.coder.foottopbusiness.presentation.main.stadium.addpitch.TimePickerDialog(
                initialHour = initialHour,
                initialMinute = initialMinute,
                onDismiss = { showStartTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.handleEvent(StadiumDetailsContract.Event.PitchStartTimeChanged("${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"))
                    showStartTimePicker = false
                }
            )
        }

        if (showEndTimePicker) {
            val initialHour = try { state.pitchEndTime.split(":")[0].toInt() } catch (_: Exception) { 18 }
            val initialMinute = try { state.pitchEndTime.split(":")[1].toInt() } catch (_: Exception) { 0 }
            uz.coder.foottopbusiness.presentation.main.stadium.addpitch.TimePickerDialog(
                initialHour = initialHour,
                initialMinute = initialMinute,
                onDismiss = { showEndTimePicker = !showEndTimePicker },
                onConfirm = { h, m ->
                    viewModel.handleEvent(StadiumDetailsContract.Event.PitchEndTimeChanged("${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"))
                    showEndTimePicker = !showEndTimePicker
                }
            )
        }

        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissAddPitchDialog) },
            title = { Text("Pitch qo'shish", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
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
                        onValueChange = { },
                        label = { Text("Boshlanish vaqti") },
                        modifier = Modifier.fillMaxWidth().clickable { showStartTimePicker = !showStartTimePicker },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = state.pitchEndTime,
                        onValueChange = { },
                        label = { Text("Tugash vaqti") },
                        modifier = Modifier.fillMaxWidth().clickable { showEndTimePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.SavePitch) }) {
                    Text("Saqlash", color = MaterialTheme.colorScheme.primary)
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
            title = { 
                val startTime = slot.start.formatToTime()
                val endTime = slot.end.formatToTime()
                Text("Vaqt: $startTime - $endTime", color = MaterialTheme.colorScheme.primary) 
            },
            text = {
                Column {
                    Text("Holati: ${slot.status?.lowercase() ?: "Noma'lum"}")
                    Spacer(Modifier.height(8.dp))
                    Text("Ushbu vaqtni band qilmoqchimisiz yoki holatini o'zgartirmoqchimisiz?")
                }
            },
            confirmButton = {
                if (slot.status == "AVAILABLE") {
                    Button(
                        onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissSlotDialog) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Tushunarli", color = MaterialTheme.colorScheme.onPrimary)
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
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
