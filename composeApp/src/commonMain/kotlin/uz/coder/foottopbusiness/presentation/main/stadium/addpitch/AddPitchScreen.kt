package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.core.ui.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPitchScreen(viewModel: AddPitchViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val hostState = remember { SnackbarHostState() }

    val descFocus = remember { FocusRequester() }
    val capacityFocus = remember { FocusRequester() }
    val priceFocus = remember { FocusRequester() }

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
        topBar = {
            TopAppBar(
                title = { Text("Yangi stadion qo'shish", color = Primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // --- IMAGE UPLOAD & PREVIEW SECTION ---
            Text("Stadion muqovasi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.05f))
                    .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (state.imageUrl.isBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, tint = Primary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Rasm URL manzilini kiriting", color = Primary, fontSize = 14.sp)
                    }
                } else {
                    var isError by remember(state.imageUrl) { mutableStateOf(false) }
                    var isLoading by remember(state.imageUrl) { mutableStateOf(true) }

                    AsyncImage(
                        model = state.imageUrl,
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onSuccess = { isLoading = false; isError = false },
                        onError = { isLoading = false; isError = true }
                    )
                    
                    if (isLoading) CircularProgressIndicator(color = Primary)
                    
                    // Status overlay
                    Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.TopEnd) {
                        Surface(
                            color = (if (isError) Color.Red else Color(0xFF4CAF50)).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (isError) Icons.Default.Error else Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isError) "URL noto'g'ri" else "Internetda ko'rinadi", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.imageUrl,
                onValueChange = { viewModel.handleEvent(AddPitchContract.Event.ImageUrl(it)) },
                label = { Text("Rasm manzili (HTTP URL)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = { Text("https://...") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
            )
            
            Spacer(Modifier.height(24.dp))
            Text("Stadion ma'lumotlari", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
            Spacer(Modifier.height(12.dp))

            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.handleEvent(AddPitchContract.Event.Name(it)) },
                label = { Text("Stadion nomi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descFocus.requestFocus() })
            )
            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.handleEvent(AddPitchContract.Event.Description(it)) },
                label = { Text("Tavsif") },
                modifier = Modifier.fillMaxWidth().focusRequester(descFocus),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
            )
            Spacer(Modifier.height(12.dp))

            // Region & District Dropdowns
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Region
                Box(Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = state.showRegionDropdown,
                        onExpandedChange = { viewModel.handleEvent(AddPitchContract.Event.ShowRegionDropdown(it)) }
                    ) {
                        OutlinedTextField(
                            value = state.selectedRegion?.name ?: "Viloyat",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showRegionDropdown) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = state.showRegionDropdown,
                            onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.ShowRegionDropdown(false)) }
                        ) {
                            state.regions.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region.name) },
                                    onClick = { viewModel.handleEvent(AddPitchContract.Event.SelectRegion(region)) }
                                )
                            }
                        }
                    }
                }
                // District
                Box(Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = state.showDistrictDropdown,
                        onExpandedChange = { if (state.selectedRegion != null) viewModel.handleEvent(AddPitchContract.Event.ShowDistrictDropdown(it)) }
                    ) {
                        OutlinedTextField(
                            value = state.selectedDistrict?.name ?: "Tuman",
                            onValueChange = {},
                            readOnly = true,
                            enabled = state.selectedRegion != null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDistrictDropdown) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = state.showDistrictDropdown && state.districts.isNotEmpty(),
                            onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.ShowDistrictDropdown(false)) }
                        ) {
                            state.districts.forEach { district ->
                                DropdownMenuItem(
                                    text = { Text(district.name?:"") },
                                    onClick = { viewModel.handleEvent(AddPitchContract.Event.SelectDistrict(district)) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Capacity + Price
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.capacity,
                    onValueChange = { viewModel.handleEvent(AddPitchContract.Event.Capacity(it)) },
                    label = { Text("Sig'imi") },
                    modifier = Modifier.weight(1f).focusRequester(capacityFocus),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { priceFocus.requestFocus() })
                )
                OutlinedTextField(
                    value = state.pricePerHour,
                    onValueChange = { viewModel.handleEvent(AddPitchContract.Event.PricePerHour(it)) },
                    label = { Text("Narxi/soat") },
                    modifier = Modifier.weight(1f).focusRequester(priceFocus),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }
            Spacer(Modifier.height(12.dp))

            // Time Selection
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimePickerField("Ochilishi", state.openTime, Modifier.weight(1f)) {
                    showOpenTimePicker = true
                }
                TimePickerField("Yopilishi", state.closeTime, Modifier.weight(1f)) {
                    showCloseTimePicker = true
                }
            }
            Spacer(Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = { viewModel.handleEvent(AddPitchContract.Event.Save) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Stadionni saqlash", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(40.dp))
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
                Text("OK", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
            }
        }
    )
}

@Composable
fun TimePickerField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AccessTime, contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Primary
                )
                Text(
                    text = value.ifBlank { "Vaqtni tanlang" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
