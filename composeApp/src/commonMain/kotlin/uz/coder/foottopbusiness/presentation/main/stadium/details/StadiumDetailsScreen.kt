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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.platform.makePhoneCall
import uz.coder.foottopbusiness.data.network.dto.stadium.SlotDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.stadium.edit.EditStadiumVoyager

// ─── Utilities ──────────────────────────────────────────────────────────────

fun String?.toLocalDateTimeSafe(): LocalDateTime? {
    if (this == null) return null
    return try {
        if (this.contains("T")) {
            LocalDateTime.parse(this)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

fun canFitDuration(
    slots: List<SlotDto>,
    startIndex: Int,
    tz: TimeZone,
    now: kotlin.time.Instant,
    durationMins: Int
): Boolean {
    val needed = slotsNeededForDuration(durationMins)
    val endIndex = startIndex + needed

    if (endIndex > slots.size) return false

    val selectedSlots = (startIndex until endIndex).mapNotNull { slots.getOrNull(it) }

    if (selectedSlots.size < needed) return false

    // All slots except the last one must be AVAILABLE and not expired
    return selectedSlots.dropLast(1).all { slot ->
        val startInstant = slot.start?.toLocalDateTimeSafe()?.toInstant(tz) ?: return false
        val isExpired = startInstant <= now
        slot.status == "AVAILABLE" && !isExpired
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StadiumDetailsScreen(viewModel: StadiumDetailsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stadium = state.stadium
    val stadiums = state.stadiums
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = remember { SnackbarHostState() }
    val tz = TimeZone.currentSystemDefault()
    val now = remember { kotlin.time.Clock.System.now() }

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
                is StadiumDetailsContract.Effect.ShowBookingResult -> {
                    // This is handled by showBookingResultDialog in state
                }
            }
        }
    }

    // Auto-selection logic
    var hasAutoSelected by remember { mutableStateOf(false) }
    val durationMins = durationMinutesKey(state.selectedDurationKey)

    LaunchedEffect(stadiums, state.selectedDate, durationMins) {
        if (state.selectedPitchIndex == null && !hasAutoSelected && stadiums.isNotEmpty()) {
            stadiums.forEachIndexed { pitchIdx, st ->
                val slots = st.slots ?: emptyList()
                val firstValidIdx = slots.indexOfFirst { slot ->
                    val slotIdx = slots.indexOf(slot)
                    val slotInstant = slot.start?.toLocalDateTimeSafe()?.toInstant(tz)
                    val expired = slotInstant?.let { it <= now } ?: true
                    slot.status == "AVAILABLE" && !expired && canFitDuration(slots, slotIdx, tz, now, durationMins)
                }
                if (firstValidIdx >= 0) {
                    hasAutoSelected = true
                    viewModel.handleEvent(StadiumDetailsContract.Event.SelectSlotSelection(pitchIdx, firstValidIdx))
                    return@LaunchedEffect
                }
            }
        }
    }

    val selectedPitchIndex = state.selectedPitchIndex
    val startIdx = state.selectedStartIndex
    val showSticky = selectedPitchIndex != null && startIdx != null
    val isOwnerless = stadium?.ownerId == null
    val isStaff = state.userRole == UserRole.SUPER_ADMIN || state.userRole == UserRole.DISTRICT_ADMIN
    val isOwner = state.userRole == UserRole.OWNER
    val canEdit = isStaff || isOwner

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
                    if (stadium != null && canEdit) {
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
        },
        bottomBar = {
            if (stadium != null) {
                if (isOwnerless) {
                    // Ownerless stadium - show Call CTA
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Button(
                                onClick = {
                                    stadium.phone?.let { makePhoneCall(it) }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            ) {
                                Icon(Icons.Default.Phone, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Telefon orqali bog'lanish",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                } else if (showSticky) {
                    val currentStadium = stadiums.getOrNull(selectedPitchIndex) ?: stadium
                    val price = currentStadium.pricePerHour ?: 0.0
                    val totalPrice = price * (durationMins / 60.0)
                    val currentSlots = currentStadium.slots ?: emptyList()
                    val selectedSlot = currentSlots.getOrNull(startIdx)
                    val startTime = selectedSlot?.start?.toLocalDateTimeSafe()
                    
                    // Calculate end time string based on duration
                    val slotsNeeded = slotsNeededForDuration(durationMins)
                    val endSlot = currentSlots.getOrNull(startIdx + slotsNeeded - 1)
                    
                    val startTimeStr = startTime?.let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" } ?: ""
                    val endTimeStr = endSlot?.end?.formatToTime() ?: ""

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.handleEvent(
                                        StadiumDetailsContract.Event.CreateBooking(
                                            stadiumId = currentStadium.id ?: 0,
                                            startTime = selectedSlot?.start ?: "",
                                            endTime = endSlot?.end ?: "",
                                            price = totalPrice
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            ) {
                                Text(
                                    text = "Bron qilish $startTimeStr – $endTimeStr",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
{ paddingValues ->
        if (stadium == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                item {
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
                }

                item {
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
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoCard(
                                icon = Icons.Default.Schedule,
                                title = "Ish vaqti",
                                subtitle = "${stadium.openTime.formatToTime()} - ${stadium.closeTime.formatToTime()}",
                                modifier = Modifier.weight(1f)
                            )
                            InfoCard(
                                icon = Icons.Default.Payments,
                                title = "Narx",
                                subtitle = "${stadium.pricePerHour?.toInt() ?: 0} so'm/soat",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (!isOwnerless) {
                            Spacer(Modifier.height(24.dp))
                            SectionTitle("Kunni tanlang")
                            Spacer(Modifier.height(10.dp))
                            DaySelector(state.selectedDate) { newDate ->
                                viewModel.handleEvent(StadiumDetailsContract.Event.SelectDate(newDate))
                            }

                            Spacer(Modifier.height(24.dp))
                            SectionTitle("Davomiylik")
                            Spacer(Modifier.height(10.dp))
                            DurationSelector(state.selectedDurationKey) { key ->
                                viewModel.handleEvent(StadiumDetailsContract.Event.ClearSelection)
                                viewModel.handleEvent(StadiumDetailsContract.Event.SelectDuration(key))
                            }

                            Spacer(Modifier.height(24.dp))
                            SectionTitle("Bo'sh vaqtlar")
                            Spacer(Modifier.height(12.dp))

                            if (state.isSlotsLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else if (stadiums.isNotEmpty()) {
                                // Find earliest available slot across all pitches
                                val earliestSlot = stadiums
                                    .flatMap { it.slots ?: emptyList() }
                                    .filter {
                                        state.selectedDate == null || it.start?.startsWith(state.selectedDate!!) == true
                                    }
                                    .let { allSlots ->
                                        allSlots.firstOrNull { slot ->
                                            val slotIdx = allSlots.indexOf(slot)
                                            val slotInstant =
                                                slot.start?.toLocalDateTimeSafe()?.toInstant(tz)
                                            val expired = slotInstant?.let { it <= now } ?: true
                                            slot.status == "AVAILABLE" && !expired && canFitDuration(
                                                allSlots,
                                                slotIdx,
                                                tz,
                                                now,
                                                durationMins
                                            )
                                        }
                                    }

                                if (earliestSlot != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Eng yaqin bo'sh vaqt: ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = earliestSlot.start.formatToTime(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }

                                // Pitch selector (tabs)
                                var selectedTabIndex by remember { mutableStateOf(0) }
                                LaunchedEffect(selectedPitchIndex) {
                                    if (selectedPitchIndex != null) selectedTabIndex =
                                        selectedPitchIndex
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(end = 16.dp)
                                ) {
                                    items(stadiums.size) { index ->
                                        val isActive = selectedTabIndex == index
                                        Surface(
                                            modifier = Modifier.clickable {
                                                selectedTabIndex = index
                                                viewModel.handleEvent(StadiumDetailsContract.Event.ClearSelection)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        ) {
                                            Text(
                                                text = "Maydon ${index + 1}",
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                ),
                                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                val currentPitchSlots =
                                    stadiums.getOrNull(selectedTabIndex)?.slots ?: emptyList()
                                if (currentPitchSlots.isEmpty()) {
                                    Text(
                                        "Bu kunda slot yo'q",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    val slotsNeeded = slotsNeededForDuration(durationMins)
                                    SlotRow(
                                        slots = currentPitchSlots,
                                        pitchIndex = selectedTabIndex,
                                        selectedPitchIndex = state.selectedPitchIndex,
                                        selectedStartIndex = state.selectedStartIndex,
                                        slotsPerBooking = slotsNeeded,
                                        durationMins = durationMins,
                                        now = now,
                                        tz = tz,
                                        onSelectStart = { idx ->
                                            viewModel.handleEvent(
                                                StadiumDetailsContract.Event.SelectSlotSelection(
                                                    selectedTabIndex,
                                                    idx
                                                )
                                            )
                                        },
                                        onClear = {
                                            viewModel.handleEvent(StadiumDetailsContract.Event.ClearSelection)
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    "Ma'lumot mavjud emas",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Bu stadion uchun onlayn bron mavjud emas. Iltimos, telefon orqali bog'laning.",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        SectionTitle("Tavsif")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stadium.description ?: "Tavsif mavjud emas.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(Modifier.height(32.dp))

                        // Add Pitch Button
                        if (canEdit) {
                            Button(
                                onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.ShowAddPitchDialog) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
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
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary
                                ),
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Stadionni tahrirlash",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(80.dp))
                    }
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
    // Booking Result Dialog
    if (state.showBookingResultDialog) {
        BookingResultDialog(
            message = state.bookingResultMessage,
            isSuccess = state.isBookingSuccess,
            onDismiss = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissBookingResultDialog) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingResultDialog(message: String, isSuccess: Boolean, onDismiss: () -> Unit) {
    val iconBgColor = if (isSuccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    val iconColor = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val buttonColor = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.Done else Icons.Default.Close,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (isSuccess) "Muvaffaqiyatli!" else "Xatolik yuz berdi",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = iconColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            ) {
                Text(
                    if (isSuccess) "Tushundim" else "Yopish",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun SlotRow(
    slots: List<SlotDto>,
    pitchIndex: Int,
    selectedPitchIndex: Int?,
    selectedStartIndex: Int?,
    slotsPerBooking: Int,
    durationMins: Int,
    now: Instant,
    tz: TimeZone,
    onSelectStart: (Int) -> Unit,
    onClear: () -> Unit
) {
    val isSelectedPitch = selectedPitchIndex == pitchIndex

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(MaterialTheme.colorScheme.primary, "Bo'sh")
            LegendDot(MaterialTheme.colorScheme.error, "Band")
            LegendDot(MaterialTheme.colorScheme.outline, "O'tgan")
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 600.dp)
        ) {
            items(slots.size) { idx ->
                val slot = slots[idx]
                val startInstant = slot.start?.toLocalDateTimeSafe()?.toInstant(tz)
                val isExpired = startInstant?.let { it <= now } ?: true

                val isStart = isSelectedPitch && selectedStartIndex == idx
                val isInRange = if (isSelectedPitch && selectedStartIndex != null) {
                    val end = selectedStartIndex + slotsPerBooking - 1
                    idx in (selectedStartIndex + 1)..end
                } else false

                val isBooked = slot.status == "BOOKED"

                val cardColor = when {
                    isStart || isInRange -> MaterialTheme.colorScheme.primary
                    isBooked -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                    isExpired -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val contentColor = when {
                    isStart || isInRange -> MaterialTheme.colorScheme.onPrimary
                    isBooked -> MaterialTheme.colorScheme.error
                    isExpired -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.onSurface
                }

                val borderStroke = when {
                    isStart || isInRange -> null
                    isBooked -> androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    else -> androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (isBooked || isExpired) return@clickable
                            
                            if (isStart || isInRange) {
                                onClear()
                            } else {
                                if (canFitDuration(slots, idx, tz, now, durationMins)) {
                                    onSelectStart(idx)
                                }
                            }
                        },
                    color = cardColor,
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = slot.start.formatToTime(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = contentColor
                        )
                        val statusLabel = when {
                            isStart -> "Start"
                            isInRange -> "Tanlangan"
                            isBooked -> "Band"
                            isExpired -> "O'tgan"
                            else -> "Bo'sh"
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun DaySelector(selectedDate: String?, onSelect: (String) -> Unit) {
    val now = remember { 
        kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 16.dp)) {
        items(7) { i ->
            val date = now.date.plus(i, DateTimeUnit.DAY)
            val dateStr = date.toString()
            val isSelected = selectedDate == dateStr
            Surface(
                modifier = Modifier.clickable { onSelect(dateStr) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(date.dayOfWeek.name.take(3), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text(date.dayOfMonth.toString(), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                }
            }
        }
    }
}

@Composable
fun DurationSelector(selectedKey: String, onSelect: (String) -> Unit) {
    val options = listOf("60" to "SIXTY", "90" to "NINETY", "120" to "ONE_HUNDRED_TWENTY")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (label, key) ->
            val isSelected = selectedKey == key
            Surface(
                modifier = Modifier.weight(1f).clickable { onSelect(key) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "$label min",
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
}
