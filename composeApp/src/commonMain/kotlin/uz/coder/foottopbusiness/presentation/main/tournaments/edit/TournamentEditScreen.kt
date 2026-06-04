package uz.coder.foottopbusiness.presentation.main.tournaments.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.visualTransformation.AmountTransformation
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsContract
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.edit.components.LocationPicker
import uz.coder.foottopbusiness.presentation.main.stadium.edit.MapSelectionScreen
import kotlin.time.Instant

class TournamentEditScreen(private val tournament: TournamentResponseDto) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<TournamentsViewModel>()
        val state by viewModel.state.collectAsState()
        val strings = Localization.current

        var name by remember { mutableStateOf(tournament.name ?: "") }
        
        fun formatFromApi(d: String?): String {
            if (d == null) return ""
            val parts = d.split("-")
            return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else d
        }
        
        var startDate by remember { mutableStateOf(formatFromApi(tournament.startDate)) }
        var endDate by remember { mutableStateOf(formatFromApi(tournament.endDate)) }
        var startTime by remember { mutableStateOf(tournament.startTime ?: "") }
        var endTime by remember { mutableStateOf(tournament.endTime ?: "") }
        var maxTeams by remember { mutableStateOf(tournament.maxTeams?.toString() ?: "") }
        var entryFee by remember { mutableStateOf(tournament.entryFee?.toInt()?.toString() ?: "") }
        
        // Handle address extraction if it's formatted as "Region, District, Address"
        val initialAddress = tournament.address ?: ""
        val addressParts = initialAddress.split(", ")
        var preciseAddress by remember { mutableStateOf(if (addressParts.size >= 3) addressParts.last() else initialAddress) }
        var latitude by remember { mutableStateOf(tournament.location?.latitude) }
        var longitude by remember { mutableStateOf(tournament.location?.longitude) }

        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        var showStartTimePicker by remember { mutableStateOf(false) }
        var showEndTimePicker by remember { mutableStateOf(false) }

        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = !showStartDatePicker },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
                            startDate = "${date.day.toString().padStart(2, '0')}.${date.month.number.toString().padStart(2, '0')}.${date.year}"
                        }
                        showStartDatePicker = !showStartDatePicker
                    }) { Text(strings.save) }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = !showStartDatePicker }) { Text(strings.cancel) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = !showEndDatePicker },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
                            endDate = "${date.day.toString().padStart(2, '0')}.${date.month.number.toString().padStart(2, '0')}.${date.year}"
                        }
                        showEndDatePicker = !showEndDatePicker
                    }) { Text(strings.save) }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = !showEndDatePicker }) { Text(strings.cancel) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showStartTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = tournament.startTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 0,
                initialMinute = tournament.startTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0,
                is24Hour = true
            )
            TimePickerDialog(
                onDismissRequest = { showStartTimePicker = !showStartTimePicker },
                confirmButton = {
                    TextButton(onClick = {
                        val h = timePickerState.hour.toString().padStart(2, '0')
                        val m = timePickerState.minute.toString().padStart(2, '0')
                        startTime = "$h:$m"
                        showStartTimePicker = !showStartTimePicker
                    }) { Text(strings.save) }
                },
                dismissButton = {
                    TextButton(onClick = { showStartTimePicker = !showStartTimePicker }) { Text(strings.cancel) }
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }

        if (showEndTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = tournament.endTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 0,
                initialMinute = tournament.endTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0,
                is24Hour = true
            )
            TimePickerDialog(
                onDismissRequest = { showEndTimePicker = !showEndTimePicker },
                confirmButton = {
                    TextButton(onClick = {
                        val h = timePickerState.hour.toString().padStart(2, '0')
                        val m = timePickerState.minute.toString().padStart(2, '0')
                        endTime = "$h:$m"
                        showEndTimePicker = !showEndTimePicker
                    }) { Text(strings.save) }
                },
                dismissButton = {
                    TextButton(onClick = { showEndTimePicker = !showEndTimePicker }) { Text(strings.cancel) }
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                )
                            )
                        )
                        .padding(top = statusBarPadding, start = 24.dp, end = 24.dp, bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { navigator.pop() },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                strings.edit,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                tournament.name ?: "",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Info Card
                CreateCard(title = strings.management, icon = Icons.Default.EmojiEvents) {
                    TournamentInputField(
                        value = name,
                        onValueChange = { name = it },
                        label = strings.tournamentName,
                        icon = Icons.Default.Edit,
                        placeholder = strings.titleHint
                    )

                    RegionDropdown(state, viewModel)
                    DistrictDropdown(state, viewModel)

                    TournamentInputField(
                        value = preciseAddress,
                        onValueChange = { preciseAddress = it },
                        label = strings.preciseAddress,
                        icon = Icons.Default.LocationOn,
                        placeholder = strings.addressPlaceholder
                    )
                }

                // Location Card
                CreateCard(title = strings.location, icon = Icons.Default.LocationOn) {
                    LocationPicker(
                        latitude = latitude,
                        longitude = longitude,
                        address = preciseAddress,
                        onLatitudeChange = { latitude = it.toDoubleOrNull() },
                        onLongitudeChange = { longitude = it.toDoubleOrNull() },
                        onAddressChange = { preciseAddress = it },
                        onSelectOnMap = {
                            navigator.push(MapSelectionScreen(latitude, longitude) { lat, lng ->
                                latitude = lat
                                longitude = lng
                            })
                        },
                        onGetCurrentLocation = {
                            viewModel.handleEvent(TournamentsContract.Event.GetCurrentLocation)
                        }
                    )
                }

                LaunchedEffect(state.latitude, state.longitude) {
                    if (state.latitude != null && state.longitude != null) {
                        latitude = state.latitude
                        longitude = state.longitude
                    }
                }

                // Date and Time Card
                CreateCard(title = "${strings.tournamentDate} & ${strings.tournamentTime}", icon = Icons.Default.CalendarToday) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).clickable { showStartDatePicker = true }) {
                            TournamentInputField(
                                value = startDate,
                                onValueChange = { },
                                label = strings.active,
                                icon = Icons.Default.CalendarMonth,
                                enabled = false,
                                placeholder = "KK.OO.YYYY"
                            )
                        }
                        Box(modifier = Modifier.weight(1f).clickable { showEndDatePicker = true }) {
                            TournamentInputField(
                                value = endDate,
                                onValueChange = { },
                                label = strings.inactive,
                                icon = Icons.Default.CalendarMonth,
                                enabled = false,
                                placeholder = "KK.OO.YYYY"
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).clickable { showStartTimePicker = true }) {
                            TournamentInputField(
                                value = startTime,
                                onValueChange = { },
                                label = strings.openTime,
                                icon = Icons.Default.AccessTime,
                                enabled = false,
                                placeholder = "00:00"
                            )
                        }
                        Box(modifier = Modifier.weight(1f).clickable { showEndTimePicker = true }) {
                            TournamentInputField(
                                value = endTime,
                                onValueChange = { },
                                label = strings.closeTime,
                                icon = Icons.Default.AccessTime,
                                enabled = false,
                                placeholder = "00:00"
                            )
                        }
                    }
                }

                // Conditions Card
                CreateCard(title = strings.technicalInfo, icon = Icons.Default.Settings) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TournamentInputField(
                            value = maxTeams,
                            onValueChange = { maxTeams = it.filter { c -> c.isDigit() } },
                            label = strings.participants,
                            icon = Icons.Default.Groups,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            placeholder = "16"
                        )
                        TournamentInputField(
                            value = entryFee,
                            onValueChange = { entryFee = it.filter { c -> c.isDigit() || c == '.' } },
                            label = strings.entryFee,
                            icon = Icons.Default.Payments,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f),
                            placeholder = "200 000",
                            visualTransformation = AmountTransformation()
                        )
                    }
                }

                if (state.error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                state.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        fun formatToApi(d: String): String {
                            val parts = d.split(".")
                            return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else d
                        }
                        
                        val formattedAddress = buildString {
                            state.selectedRegion?.let { append(it.name) }
                            state.selectedDistrict?.let { 
                                if (isNotEmpty()) append(", ")
                                append(it.name) 
                            }
                            if (preciseAddress.isNotBlank()) {
                                if (isNotEmpty()) append(", ")
                                append(preciseAddress)
                            }
                        }

                        viewModel.handleEvent(
                            TournamentsContract.Event.Update(
                                id = tournament.id ?: 0L,
                                name = name,
                                startDate = formatToApi(startDate),
                                endDate = formatToApi(endDate),
                                maxTeams = maxTeams.toIntOrNull() ?: 0,
                                entryFee = entryFee.toDoubleOrNull() ?: 0.0,
                                address = formattedAddress.takeIf { it.isNotBlank() },
                                startTime = startTime.takeIf { it.isNotBlank() },
                                endTime = endTime.takeIf { it.isNotBlank() },
                                latitude = latitude,
                                longitude = longitude
                            )
                        )
                        navigator.pop()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    enabled = !state.isCreating && name.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(strings.save.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    @Composable
    private fun CreateCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                content()
            }
        }
    }

    @Composable
    private fun TournamentInputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        icon: ImageVector,
        modifier: Modifier = Modifier,
        placeholder: String = "",
        keyboardType: KeyboardType = KeyboardType.Text,
        enabled: Boolean = true,
        visualTransformation: VisualTransformation = VisualTransformation.None
    ) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 1)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 12.sp) },
                leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                enabled = enabled,
                singleLine = true,
                visualTransformation = visualTransformation,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RegionDropdown(
        state: TournamentsContract.State,
        viewModel: TournamentsViewModel
    ) {
        val strings = Localization.current
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.chooseRegion, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            ExposedDropdownMenuBox(
                expanded = state.showRegionDropdown,
                onExpandedChange = { viewModel.handleEvent(TournamentsContract.Event.ShowRegionDropdown(it)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.selectedRegion?.name ?: strings.chooseRegion,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showRegionDropdown) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(
                    expanded = state.showRegionDropdown,
                    onDismissRequest = { viewModel.handleEvent(TournamentsContract.Event.ShowRegionDropdown(false)) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    state.regions.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region.name) },
                            onClick = {
                                viewModel.handleEvent(TournamentsContract.Event.SelectRegion(region))
                            }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DistrictDropdown(
        state: TournamentsContract.State,
        viewModel: TournamentsViewModel
    ) {
        val strings = Localization.current
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.chooseDistrict, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            ExposedDropdownMenuBox(
                expanded = state.showDistrictDropdown,
                onExpandedChange = { viewModel.handleEvent(TournamentsContract.Event.ShowDistrictDropdown(it)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.selectedDistrict?.name ?: strings.chooseDistrict,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDistrictDropdown) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(
                    expanded = state.showDistrictDropdown,
                    onDismissRequest = { viewModel.handleEvent(TournamentsContract.Event.ShowDistrictDropdown(false)) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    state.districts.forEach { district ->
                        DropdownMenuItem(
                            text = { Text(district.name ?: "") },
                            onClick = {
                                viewModel.handleEvent(TournamentsContract.Event.SelectDistrict(district))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                content()
            }
        }
    )
}
