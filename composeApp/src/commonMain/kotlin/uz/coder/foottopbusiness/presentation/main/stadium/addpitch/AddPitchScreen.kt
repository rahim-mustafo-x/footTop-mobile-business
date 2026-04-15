package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPitchScreen(viewModel: AddPitchViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }

    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddPitchContract.Effect.NavigateBack -> onBack()
                is AddPitchContract.Effect.ShowToast -> {
                    log("AddPitch", "Effect message: ${effect.message}")
                    hostState.showSnackbar(effect.message)
                }
            }
        }
    }

    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }

    if (showOpenTimePicker) {
        val initialHour = try { state.openTime.split(":")[0].toInt() } catch (_: Exception) { 8 }
        val initialMinute = try { state.openTime.split(":")[1].toInt() } catch (_: Exception) { 0 }
        
        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = { showOpenTimePicker = !showOpenTimePicker },
            onConfirm = { hour, minute ->
                val t = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                viewModel.handleEvent(AddPitchContract.Event.OpenTime(t))
                showOpenTimePicker = !showOpenTimePicker
            }
        )
    }

    if (showCloseTimePicker) {
        val initialHour = try { state.closeTime.split(":")[0].toInt() } catch (_: Exception) { 22 }
        val initialMinute = try { state.closeTime.split(":")[1].toInt() } catch (_: Exception) { 0 }
        
        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = { showCloseTimePicker = !showCloseTimePicker },
            onConfirm = { hour, minute ->
                val t = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                viewModel.handleEvent(AddPitchContract.Event.CloseTime(t))
                showCloseTimePicker = !showCloseTimePicker
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState) },
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = statusBarPadding, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Yangi stadion",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Maydon haqida",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    RegionDropdown(state, viewModel)
                }
                Box(modifier = Modifier.weight(1f)) {
                    DistrictDropdown(state, viewModel)
                }
            }

            LabelAndField("ANIQ MANZIL", state.name, "Ko'cha, uy raqami") {
                viewModel.handleEvent(AddPitchContract.Event.Name(it))
            }

            LabelAndField("TAVSIF (DESCRIPTION)", state.description, "Stadion haqida qo'shimcha ma'lumot...", singleLine = false) {
                viewModel.handleEvent(AddPitchContract.Event.Description(it))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    LabelAndField("MAYDONLAR SONI", state.capacity, "3", KeyboardType.Number) {
                        viewModel.handleEvent(AddPitchContract.Event.Capacity(it))
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    SportTypeDropdown(state, viewModel)
                }
            }

            LabelAndField("SOATLIK NARX (SO'M)", state.pricePerHour, "50 000", KeyboardType.Number) {
                viewModel.handleEvent(AddPitchContract.Event.PricePerHour(it))
            }

            Column {
                Text("ISH VAQTI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        state.openTime.ifBlank { "08:00" },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { showOpenTimePicker = true }.weight(1f)
                    )
                    Text("-", modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        state.closeTime.ifBlank { "22:00" },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { showCloseTimePicker = true }.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            Column {
                Text("RASM YUKLASH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { /* TODO: Image picker */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Rasm tanlang")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.handleEvent(AddPitchContract.Event.Save) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Saqlash", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LabelAndField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            maxLines = if (singleLine) 1 else 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionDropdown(
    state: AddPitchContract.State,
    viewModel: AddPitchViewModel
) {
    Column {
        Text("VILOYAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showRegionDropdown,
            onExpandedChange = { viewModel.handleEvent(AddPitchContract.Event.ShowRegionDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedRegion?.name ?: "Tanlang",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showRegionDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = state.showRegionDropdown,
                onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.ShowRegionDropdown(false)) }
            ) {
                state.regions.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(region.name ?: "") },
                        onClick = {
                            viewModel.handleEvent(AddPitchContract.Event.SelectRegion(region))
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
    state: AddPitchContract.State,
    viewModel: AddPitchViewModel
) {
    Column {
        Text("TUMAN/SHAHAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showDistrictDropdown,
            onExpandedChange = { viewModel.handleEvent(AddPitchContract.Event.ShowDistrictDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedDistrict?.name ?: "Tanlang",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDistrictDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = state.showDistrictDropdown,
                onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.ShowDistrictDropdown(false)) }
            ) {
                state.districts.forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district.name ?: "") },
                        onClick = {
                            viewModel.handleEvent(AddPitchContract.Event.SelectDistrict(district))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportTypeDropdown(
    state: AddPitchContract.State,
    viewModel: AddPitchViewModel
) {
    Column {
        Text("SPORT TURI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showTypeDropdown,
            onExpandedChange = { viewModel.handleEvent(AddPitchContract.Event.ShowTypeDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.type.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showTypeDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = state.showTypeDropdown,
                onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.ShowTypeDropdown(false)) }
            ) {
                StadiumType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = {
                            viewModel.handleEvent(AddPitchContract.Event.Type(type))
                            viewModel.handleEvent(AddPitchContract.Event.ShowTypeDropdown(false))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish", color = MaterialTheme.colorScheme.secondary) }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    )
}
