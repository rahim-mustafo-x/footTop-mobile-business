@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.stadium.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.platform.makePhoneCall
import uz.coder.foottopbusiness.core.plusMinutes
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.ui.shimmer
import uz.coder.foottopbusiness.data.network.dto.stadium.SlotDto
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.stadium.edit.EditStadiumVoyager
import kotlin.time.Clock
import uz.coder.foottopbusiness.core.platform.NotificationPermissionLauncher
import uz.coder.foottopbusiness.core.ui.Success

// --- Slot state enum ---
private enum class SlotRowState {
    AVAILABLE, SELECTED, BOOKED, PAST
}

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Expected input: 991234567
        // Output: (99) 123-45-67
        val trimmed = if (text.text.length >= 9) text.text.substring(0, 9) else text.text
        var out = ""
        for (i in trimmed.indices) {
            if (i == 0) out += "("
            out += trimmed[i]
            if (i == 1) out += ") "
            if (i == 4) out += "-"
            if (i == 6) out += "-"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset <= 2) return offset + 1 // (XX
                if (offset <= 5) return offset + 3 // (XX) XXX
                if (offset <= 7) return offset + 4 // (XX) XXX-XX
                if (offset <= 9) return offset + 5 // (XX) XXX-XX-XX
                return 13
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 1) return 0
                if (offset <= 4) return offset - 1
                if (offset <= 8) return offset - 3
                if (offset <= 11) return offset - 4
                if (offset <= 13) return offset - 5
                return 9
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
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
    val now: Instant = remember { Clock.System.now() }
    val scope = rememberCoroutineScope()
    val strings = Localization.current
    var showBookedSlots by remember { mutableStateOf(false) }
    var showNames by remember { mutableStateOf(false) }
    var showBookingSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                StadiumDetailsContract.Effect.NavigateBack -> onBack()
                is StadiumDetailsContract.Effect.NavigateToEdit -> {
                    navigator.push(EditStadiumVoyager(effect.stadium))
                }
                is StadiumDetailsContract.Effect.ShowToast -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(ErrorMapper.map(effect.message, strings))
                    }
                }
                is StadiumDetailsContract.Effect.ShowBookingResult -> { }
            }
        }
    }

    var hasAutoSelected by remember { mutableStateOf(false) }
    val durationMins = StadiumDetailsContract.durationMinutesKey(state.selectedDurationKey)

    LaunchedEffect(state.selectedDate, state.selectedDurationKey) {
        hasAutoSelected = false
    }

    LaunchedEffect(stadiums, state.selectedDate, durationMins) {
        if (state.selectedPitchIndex == null && !hasAutoSelected && stadiums.isNotEmpty()) {
            stadiums.forEachIndexed { pitchIdx, st ->
                val slots = st.slots ?: emptyList()
                val firstValidIdx = slots.indexOfFirst { slot ->
                    val slotInstant = slot.start.toLocalDateTimeSafe()?.toInstant(tz)
                    val expired = slotInstant?.let { it <= now } ?: true
                    slot.status == "AVAILABLE" && !expired
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (stadium != null) {
                if (isOwnerless) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Button(
                                onClick = { stadium.phone?.let { makePhoneCall(it) } },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Success),
                                modifier = Modifier.fillMaxWidth().height(60.dp)
                            ) {
                                Icon(Icons.Default.Phone, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = strings.callViaPhone,
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
                    val startTime = selectedSlot?.start.toLocalDateTimeSafe()
                    val startTimeStr = startTime?.let { formatTimeFromDateTime(it) } ?: ""
                    val selectedEnd = startTime?.plusMinutes(durationMins)
                    val endTimeStr = selectedEnd?.let { formatTimeFromDateTime(it) } ?: ""
                    Surface(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$startTimeStr – $endTimeStr", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                Column(modifier = Modifier.weight(0.6f)) {
                                    Text(strings.duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$durationMins min", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                }
                                Column(modifier = Modifier.weight(0.8f)) {
                                    Spacer(Modifier.height(16.dp))
                                    Text("${totalPrice.toInt()} so'm", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = { showBookingSheet = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(
                                    text = strings.bookNow,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (stadium == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image Shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .shimmer()
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Shimmer
                    Box(modifier = Modifier.width(200.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).shimmer())
                    // Location Shimmer
                    Box(modifier = Modifier.width(150.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                        Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Section Title Shimmer
                    Box(modifier = Modifier.width(120.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                    // Day Selector Shimmer
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(5) {
                            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.surface)
            ) {
                // Hero Image
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=1000",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))
                            )
                        )
                        IconButton(
                            onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.BackClick) },
                            modifier = Modifier.statusBarsPadding().padding(12.dp).align(Alignment.TopStart).background(Color.Black.copy(0.3f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back, tint = Color.White)
                        }

                        if (canEdit) {
                            IconButton(
                                onClick = { viewModel.handleEvent(StadiumDetailsContract.Event.EditClick) },
                                modifier = Modifier.statusBarsPadding().padding(12.dp).align(Alignment.TopEnd).background(Color.Black.copy(0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.Edit, strings.edit, tint = Color.White)
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().offset(y = (-24).dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stadium.name ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(text = stadium.regionName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = stadium.isActive == true,
                                    onCheckedChange = { viewModel.handleEvent(StadiumDetailsContract.Event.ToggleActive(it)) },
                                    enabled = !state.isUpdatingStatus,
                                    colors = SwitchDefaults.colors(checkedTrackColor = Success)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        BeautifulStadiumInfoCard(stadium, strings)

                        if (!isOwnerless) {
                            Spacer(Modifier.height(24.dp))
                            SectionTitle(strings.selectDay)
                            Spacer(Modifier.height(10.dp))
                            DaySelector(state.selectedDate) { viewModel.handleEvent(StadiumDetailsContract.Event.SelectDate(it)) }

                            Spacer(Modifier.height(24.dp))
                            SectionTitle(strings.duration)
                            Spacer(Modifier.height(10.dp))
                            DurationSelector(state.selectedDurationKey) { key ->
                                viewModel.handleEvent(StadiumDetailsContract.Event.ClearSelection)
                                viewModel.handleEvent(StadiumDetailsContract.Event.SelectDuration(key))
                            }

                            Spacer(Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    strings.dailyDetails,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(strings.booked, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                                        Switch(
                                            checked = showBookedSlots,
                                            onCheckedChange = { showBookedSlots = it },
                                            modifier = Modifier.scale(0.7f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(strings.showNames, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                                        Switch(
                                            checked = showNames,
                                            onCheckedChange = { showNames = it },
                                            modifier = Modifier.scale(0.7f)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))

                            if (state.isSlotsLoading) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .shimmer()
                                        )
                                    }
                                }
                            } else if (stadiums.isNotEmpty()) {
                                var selectedTabIndex by remember { mutableIntStateOf(0) }
                                LaunchedEffect(selectedPitchIndex) { if (selectedPitchIndex != null) selectedTabIndex = selectedPitchIndex }

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 16.dp)) {
                                    items(stadiums.size) { index ->
                                        val isActive = selectedTabIndex == index
                                        val displayName = if (showNames) {
                                            stadiums[index].name ?: stadium.name ?: "${strings.field} ${index + 1}"
                                        } else {
                                            "${strings.field} ${index + 1}"
                                        }
                                        Surface(
                                            modifier = Modifier.clickable { selectedTabIndex = index; viewModel.handleEvent(StadiumDetailsContract.Event.ClearSelection) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        ) {
                                            Text(text = displayName, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                
                                val currentStadium = stadiums[selectedTabIndex]
                                val slots = currentStadium.slots ?: emptyList()
                                
                                val nowInstant = Clock.System.now()
                                val slotsWithIndices = slots.mapIndexed { i, s -> i to s }
                                    .filter { state.selectedDate == null || it.second.start.toLocalDateTimeSafe()?.date.toString() == state.selectedDate }
                                    .filter { showBookedSlots || it.second.status != "BOOKED" }
                                
                                if (slotsWithIndices.isEmpty()) {
                                    Text(strings.noSlotsToday, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                                } else {
                                    slotsWithIndices.forEachIndexed { _, (origIdx, slot) ->
                                        val rowState = resolveSlotStateFixed(
                                            slot = slot,
                                            currentIndex = origIdx,
                                            selectedStartIndex = state.selectedStartIndex,
                                            selectedPitchIndex = state.selectedPitchIndex,
                                            pitchIndex = selectedTabIndex,
                                            tz = tz,
                                            nowInstant = nowInstant
                                        )
                                        SlotListItem(
                                            slot = slot,
                                            rowState = rowState,
                                            onClick = {
                                                if (rowState == SlotRowState.AVAILABLE || rowState == SlotRowState.SELECTED) {
                                                    if (rowState == SlotRowState.SELECTED) viewModel.handleEvent(StadiumDetailsContract.Event.ClearSelection)
                                                    else viewModel.handleEvent(StadiumDetailsContract.Event.SelectSlotSelection(selectedTabIndex, origIdx))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        SectionTitle(strings.description)
                        Text(text = stadium.description ?: strings.noDataYet, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                        
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    if (showBookingSheet && showSticky) {
        val currentStadium = stadiums.getOrNull(selectedPitchIndex) ?: stadium!!
        val price = currentStadium.pricePerHour ?: 0.0
        val totalPrice = price * (durationMins / 60.0)
        val currentSlots = currentStadium.slots ?: emptyList()
        val selectedSlot = currentSlots.getOrNull(startIdx)
        val startTime = selectedSlot?.start.toLocalDateTimeSafe()
        val startTimeStr = startTime?.let { formatTimeFromDateTime(it) } ?: ""
        val selectedEnd = startTime?.plusMinutes(durationMins)
        val endTimeStr = selectedEnd?.let { formatTimeFromDateTime(it) } ?: ""

        ModalBottomSheet(
            onDismissRequest = { showBookingSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            var showUserInfo by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = strings.bookingInfo,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${strings.selectedTime}: $startTimeStr – $endTimeStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                Surface(
                    onClick = { showUserInfo = !showUserInfo },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(strings.customerInfoOptional, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Icon(if (showUserInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                    }
                }

                if (showUserInfo) {
                    Spacer(Modifier.height(16.dp))
                    
                    val phoneError = state.showBookerErrors && state.bookerPhone.isNotEmpty() && state.bookerPhone.length < 9

                    OutlinedTextField(
                        value = state.bookerName,
                        onValueChange = { viewModel.handleEvent(StadiumDetailsContract.Event.UpdateBookerName(it)) },
                        label = { Text(strings.customerName) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = state.bookerPhone,
                        onValueChange = { if (it.length <= 9) viewModel.handleEvent(StadiumDetailsContract.Event.UpdateBookerPhone(it)) },
                        label = { Text(strings.phoneNumber) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("+998 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PhoneVisualTransformation(),
                        isError = phoneError,
                        supportingText = if (phoneError) { { Text(strings.enterFullPhone) } } else null,
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = if (phoneError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
                    )
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (selectedEnd != null) {
                            viewModel.handleEvent(
                                StadiumDetailsContract.Event.CreateBooking(
                                    stadiumId = currentStadium.id ?: 0,
                                    startTime = selectedSlot?.start ?: "",
                                    endTime = selectedEnd.toString(),
                                    price = totalPrice,
                                    name = state.bookerName.takeIf { it.isNotBlank() },
                                    phone = if (state.bookerPhone.isNotBlank()) "998${state.bookerPhone}" else null
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = "${strings.confirm} (${totalPrice.toInt()} so'm)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (state.showBookingResultDialog) {
        BookingResultDialog(message = state.bookingResultMessage, isSuccess = state.isBookingSuccess, onDismiss = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissBookingResultDialog) })
    }

    if (state.showNotificationPermissionDialog) {
        NotificationPermissionExplanationDialog(
            onConfirm = { viewModel.handleEvent(StadiumDetailsContract.Event.RequestNotificationPermission) },
            onDismiss = { viewModel.handleEvent(StadiumDetailsContract.Event.SetShowNotificationPermissionDialog(false)) }
        )
    }

    if (state.showPermanentlyDeniedDialog) {
        PermanentlyDeniedDialog(
            onOpenSettings = { viewModel.handleEvent(StadiumDetailsContract.Event.OpenSettings) },
            onDismiss = { viewModel.handleEvent(StadiumDetailsContract.Event.DismissPermanentlyDeniedDialog) }
        )
    }

    NotificationPermissionLauncher(
        trigger = state.triggerNotificationRequest,
        onResult = { status ->
            viewModel.handleEvent(StadiumDetailsContract.Event.OnNotificationPermissionResult(status))
        }
    )
}

@Composable
private fun NotificationPermissionExplanationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val strings = Localization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notificationRationaleTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.notificationRationaleDesc)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BenefitItem(strings.notificationBenefit1)
                    BenefitItem(strings.notificationBenefit2)
                    BenefitItem(strings.notificationBenefit3)
                    BenefitItem(strings.notificationBenefit4)
                }
                Text(
                    text = "${strings.enableNotifications}?",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.enableNotifications)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.maybeLater)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.Check, 
            null, 
            tint = Success, 
            modifier = Modifier.size(20.dp)
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PermanentlyDeniedDialog(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val strings = Localization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notificationsDeniedTitle) },
        text = { Text(strings.notificationsDeniedDesc) },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.openSettings)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

// --- Helpers and Sub-composables ---

private fun formatTimeFromDateTime(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

@Composable
private fun SlotListItem(
    slot: SlotDto,
    rowState: SlotRowState,
    onClick: () -> Unit
) {
    val strings = Localization.current
    val startDt = slot.start.toLocalDateTimeSafe()
    val startStr = startDt?.let { formatTimeFromDateTime(it) } ?: "--:--"
    val endDt = slot.end.toLocalDateTimeSafe()
    val endStr = endDt?.let { formatTimeFromDateTime(it) } ?: "--:--"

    val isSelected = rowState == SlotRowState.SELECTED
    val isBusy = rowState == SlotRowState.BOOKED || rowState == SlotRowState.PAST

    val statusText = when (rowState) {
        SlotRowState.AVAILABLE -> strings.statusAvailableWord
        SlotRowState.SELECTED -> strings.statusSelectedWord
        SlotRowState.BOOKED -> strings.statusBookedWord
        SlotRowState.PAST -> strings.statusPastWord
    }

    val subtitleText = when (rowState) {
        SlotRowState.SELECTED -> strings.statusSelectedSentence
        SlotRowState.BOOKED -> strings.statusBookedSentence
        SlotRowState.PAST -> strings.statusPastSentence
        SlotRowState.AVAILABLE -> strings.statusAvailableSentence
    }

    val statusColor = when (rowState) {
        SlotRowState.AVAILABLE -> Success
        SlotRowState.SELECTED -> MaterialTheme.colorScheme.primary
        SlotRowState.BOOKED -> MaterialTheme.colorScheme.error
        SlotRowState.PAST -> MaterialTheme.colorScheme.outline
    }

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        isBusy -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isBusy) { onClick() },
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (isBusy) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$startStr - $endStr",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isBusy) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor.copy(alpha = 0.8f)
                    )
                }
            }

            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = statusText.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = statusColor
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
}

@Composable
fun SwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.scale(0.7f)
            )
        }
    }
}

@Composable
fun SwipeToConfirmButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val trackWidth = 280.dp
    val thumbSize = 48.dp
    val maxPx = with(density) { (trackWidth - thumbSize).toPx() }
    
    var thumbOffset by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = modifier
            .width(trackWidth)
            .height(thumbSize)
            .background(if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f), CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(thumbOffset.toInt(), 0) }
                .size(thumbSize)
                .padding(4.dp)
                .background(if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (thumbOffset >= maxPx * 0.9f) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onConfirm()
                            }
                            thumbOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            thumbOffset = (thumbOffset + dragAmount).coerceIn(0f, maxPx)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
        }
    }
}

private fun resolveSlotStateFixed(
    slot: SlotDto,
    currentIndex: Int,
    selectedStartIndex: Int?,
    selectedPitchIndex: Int?,
    pitchIndex: Int,
    tz: TimeZone,
    nowInstant: Instant
): SlotRowState {
    val startDt = slot.start.toLocalDateTimeSafe() ?: return SlotRowState.PAST
    val startInstant = startDt.toInstant(tz)
    val isExpired = startInstant <= nowInstant

    if (isExpired) return SlotRowState.PAST
    if (slot.status == "BOOKED") return SlotRowState.BOOKED

    if (selectedPitchIndex == pitchIndex && selectedStartIndex == currentIndex) return SlotRowState.SELECTED
    
    return SlotRowState.AVAILABLE
}

@Composable
private fun BeautifulStadiumInfoCard(stadium: uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse, strings: uz.coder.foottopbusiness.core.localization.Language) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(strings.workingHours, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${stadium.openTime.toLocalDateTimeSafe()?.let { formatTimeFromDateTime(it) } ?: "08:00"} - ${stadium.closeTime.toLocalDateTimeSafe()?.let { formatTimeFromDateTime(it) } ?: "23:00"}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Success.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Payments, null, tint = Success, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(strings.price, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${stadium.pricePerHour?.toInt() ?: 0} ${strings.uzsPerHour}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun DaySelector(selectedDate: String?, onSelect: (String) -> Unit) {
    val now: LocalDateTime = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
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
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(date.dayOfWeek.name.take(3), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text(date.dayOfMonth.toString(), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text("$label min", modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingResultDialog(message: String, isSuccess: Boolean, onDismiss: () -> Unit) {
    val strings = Localization.current
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.padding(24.dp).navigationBarsPadding().padding(bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error, null, tint = if (isSuccess) Success else MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(if (isSuccess) strings.success else strings.error, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(message, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 16.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp)) { Text(strings.understand) }
        }
    }
}
