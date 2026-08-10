package uz.coder.foottopbusiness.presentation.main.tournaments.edit

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
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
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.platform.LocationPermissionLauncher
import uz.coder.foottopbusiness.core.visualTransformation.AmountTransformation
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsContract
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsViewModel
import uz.coder.foottopbusiness.presentation.main.tournaments.components.DerivedLocationSummary
import uz.coder.foottopbusiness.presentation.main.tournaments.components.StadiumMultiSelect
import uz.coder.foottopbusiness.presentation.main.stadium.edit.components.LocationPicker
import uz.coder.foottopbusiness.presentation.main.stadium.edit.MapSelectionScreen
import kotlinx.datetime.Instant

class TournamentEditScreen(private val tournament: TournamentResponseDto) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<TournamentsViewModel>()
        val state by viewModel.state.collectAsState()
        val strings = Localization.current

        LocationPermissionLauncher(
            trigger = state.triggerLocationPermission,
            onResult = { status ->
                viewModel.handleEvent(TournamentsContract.Event.OnLocationPermissionResult(status))
            }
        )

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
        
        // Manzil endi qanday saqlangan bo'lsa shundayligicha ko'rsatiladi.
        // Ilgari u split(", ").last() bilan ajratilardi - vergul ishlatilgan
        // manzilda ("Bunyodkor ko'chasi, 5-uy") faqat oxirgi bo'lak qolib,
        // qolgani tahrirlashdayoq yo'qolardi.
        var preciseAddress by remember { mutableStateOf(tournament.address ?: "") }
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
                            viewModel.handleEvent(TournamentsContract.Event.ShowErrors(false))
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
                            viewModel.handleEvent(TournamentsContract.Event.ShowErrors(false))
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
                GradientHeader(
                    title = strings.edit,
                    subtitle = tournament.name,
                    onBack = { navigator.pop() }
                )
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
                val infoError = state.showErrors && (name.isBlank() || state.selectedStadiums.isEmpty())
                PremiumEditCard(title = strings.management, icon = Icons.Outlined.EmojiEvents, isError = infoError) {
                    TournamentInputField(
                        value = name,
                        onValueChange = { name = it; viewModel.handleEvent(TournamentsContract.Event.ShowErrors(false)) },
                        label = strings.tournamentName,
                        icon = Icons.Outlined.Edit,
                        placeholder = strings.titleHint,
                        isError = state.showErrors && name.isBlank()
                    )

                    StadiumMultiSelect(
                        state,
                        viewModel,
                        isError = state.showErrors && state.selectedStadiums.isEmpty()
                    )

                    DerivedLocationSummary(state)

                    TournamentInputField(
                        value = preciseAddress,
                        onValueChange = { preciseAddress = it },
                        label = strings.preciseAddress,
                        icon = Icons.Outlined.LocationOn,
                        placeholder = strings.addressPlaceholder
                    )
                }

                // Location Card - qo'lda Lat/Long kiritish olib tashlandi,
                // koordinata stadiondan yoki xaritadan keladi
                PremiumEditCard(title = strings.locationInfo, icon = Icons.Outlined.Map) {
                    LocationPicker(
                        latitude = latitude,
                        longitude = longitude,
                        address = preciseAddress,
                        onSelectOnMap = {
                            navigator.push(MapSelectionScreen(latitude, longitude) { lat, lng ->
                                latitude = lat
                                longitude = lng
                            })
                        }
                    )
                }

                // Saqlangan stadionni ro'yxat yuklangach qayta tanlaymiz
                LaunchedEffect(state.stadiums, tournament.stadiumId) {
                    tournament.stadiumId?.let {
                        viewModel.handleEvent(TournamentsContract.Event.PrefillStadium(it))
                    }
                }

                LaunchedEffect(state.latitude, state.longitude, state.primaryStadium) {
                    if (state.latitude != null && state.longitude != null) {
                        latitude = state.latitude
                        longitude = state.longitude
                    }
                    state.primaryStadium?.location?.address?.let { preciseAddress = it }
                }

                // Date and Time Card
                val dateError = state.showErrors && (startDate.isBlank() || endDate.isBlank())
                PremiumEditCard(title = "${strings.tournamentDate} & ${strings.tournamentTime}", icon = Icons.Outlined.CalendarToday, isError = dateError) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).clickable { showStartDatePicker = true }) {
                            TournamentInputField(
                                value = startDate,
                                onValueChange = { },
                                label = strings.active,
                                icon = Icons.Outlined.CalendarMonth,
                                enabled = false,
                                placeholder = "KK.OO.YYYY",
                                isError = state.showErrors && startDate.isBlank()
                            )
                        }
                        Box(modifier = Modifier.weight(1f).clickable { showEndDatePicker = true }) {
                            TournamentInputField(
                                value = endDate,
                                onValueChange = { },
                                label = strings.inactive,
                                icon = Icons.Outlined.CalendarMonth,
                                enabled = false,
                                placeholder = "KK.OO.YYYY",
                                isError = state.showErrors && endDate.isBlank()
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).clickable { showStartTimePicker = true }) {
                            TournamentInputField(
                                value = startTime,
                                onValueChange = { },
                                label = strings.openTime,
                                icon = Icons.Outlined.AccessTime,
                                enabled = false,
                                placeholder = "00:00"
                            )
                        }
                        Box(modifier = Modifier.weight(1f).clickable { showEndTimePicker = true }) {
                            TournamentInputField(
                                value = endTime,
                                onValueChange = { },
                                label = strings.closeTime,
                                icon = Icons.Outlined.AccessTime,
                                enabled = false,
                                placeholder = "00:00"
                            )
                        }
                    }
                }

                // Conditions Card
                PremiumEditCard(title = strings.technicalInfo, icon = Icons.Outlined.Settings) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TournamentInputField(
                            value = maxTeams,
                            onValueChange = { maxTeams = it.filter { c -> c.isDigit() } },
                            label = strings.participants,
                            icon = Icons.Outlined.Groups,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            placeholder = "16"
                        )
                        TournamentInputField(
                            value = entryFee,
                            onValueChange = { entryFee = it.filter { c -> c.isDigit() || c == '.' } },
                            label = strings.entryFee,
                            icon = Icons.Outlined.Payments,
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
                        
                        // Viloyat/tuman districtId orqali ketadi - manzil
                        // matniga takroran qo'shilmaydi
                        if (name.isBlank() || startDate.isBlank() || endDate.isBlank() || state.selectedStadiums.isEmpty()) {
                            viewModel.handleEvent(TournamentsContract.Event.ShowErrors(true))
                            return@Button
                        }

                        viewModel.handleEvent(
                            TournamentsContract.Event.Update(
                                id = tournament.id ?: 0L,
                                name = name,
                                startDate = formatToApi(startDate),
                                endDate = formatToApi(endDate),
                                maxTeams = maxTeams.toIntOrNull() ?: 0,
                                entryFee = entryFee.toDoubleOrNull() ?: 0.0,
                                address = preciseAddress.takeIf { it.isNotBlank() },
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
                    enabled = !state.isCreating,
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
    private fun PremiumEditCard(title: String, icon: ImageVector, isError: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = if (isError) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = (if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
        isError: Boolean = false,
        visualTransformation: VisualTransformation = VisualTransformation.None
    ) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 1)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 13.sp) },
                leadingIcon = { Icon(icon, null, tint = (if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                enabled = enabled,
                isError = isError,
                singleLine = true,
                visualTransformation = visualTransformation,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconColor = (if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.5f),
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
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
