package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.coder.foottopbusiness.core.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPitchScreen(viewModel: AddPitchViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val descFocus = remember { FocusRequester() }
    val capacityFocus = remember { FocusRequester() }
    val priceFocus = remember { FocusRequester() }
    val imageFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddPitchContract.Effect.NavigateBack -> onBack()
                is AddPitchContract.Effect.ShowToast -> {
                    log("AddPitch", "Effect message: ${effect.message}")
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }

    if (showOpenTimePicker) {
        val pickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showOpenTimePicker = !showOpenTimePicker },
            confirmButton = {
                TextButton(onClick = {
                    val t = "${pickerState.hour.toString().padStart(2, '0')}:${pickerState.minute.toString().padStart(2, '0')}"
                    viewModel.handleEvent(AddPitchContract.Event.OpenTime(t))
                    showOpenTimePicker = !showOpenTimePicker
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showOpenTimePicker = !showOpenTimePicker }) { Text("Cancel") } },
            text = { TimePicker(state = pickerState) }
        )
    }

    if (showCloseTimePicker) {
        val pickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showCloseTimePicker = !showCloseTimePicker },
            confirmButton = {
                TextButton(onClick = {
                    val t = "${pickerState.hour.toString().padStart(2, '0')}:${pickerState.minute.toString().padStart(2, '0')}"
                    viewModel.handleEvent(AddPitchContract.Event.CloseTime(t))
                    showCloseTimePicker = !showCloseTimePicker
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCloseTimePicker = !showCloseTimePicker }) { Text("Cancel") } },
            text = { TimePicker(state = pickerState) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add A Pitch") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Spacer(Modifier.height(8.dp))

            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.handleEvent(AddPitchContract.Event.Name(it)) },
                label = { Text("Stadium Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descFocus.requestFocus() })
            )
            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.handleEvent(AddPitchContract.Event.Description(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().focusRequester(descFocus),
                shape = RoundedCornerShape(8.dp),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
            )
            Spacer(Modifier.height(12.dp))

            // Region dropdown
            ExposedDropdownMenuBox(
                expanded = state.showRegionDropdown,
                onExpandedChange = { viewModel.handleEvent(AddPitchContract.Event.ShowRegionDropdown(it)) }
            ) {
                OutlinedTextField(
                    value = state.selectedRegion?.name
                        ?: if (state.regions.isEmpty()) "Yuklanmoqda..." else "Viloyat tanlang",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Viloyat") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showRegionDropdown) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
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
            Spacer(Modifier.height(12.dp))

            // District dropdown
            ExposedDropdownMenuBox(
                expanded = state.showDistrictDropdown,
                onExpandedChange = {
                    if (state.selectedRegion != null) {
                        viewModel.handleEvent(AddPitchContract.Event.ShowDistrictDropdown(it))
                    }
                }
            ) {
                val districtText = when {
                    state.selectedDistrict != null -> state.selectedDistrict!!.name
                    state.selectedRegion == null -> "Avval viloyat tanlang"
                    state.districts.isEmpty() -> "Yuklanmoqda..."
                    else -> "Tuman tanlang"
                }?:""
                OutlinedTextField(
                    value = districtText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tuman") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDistrictDropdown) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = state.selectedRegion != null
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
            Spacer(Modifier.height(12.dp))

            // Type dropdown
            ExposedDropdownMenuBox(
                expanded = state.showTypeDropdown,
                onExpandedChange = { viewModel.handleEvent(AddPitchContract.Event.ShowTypeDropdown(it)) }
            ) {
                OutlinedTextField(
                    value = state.type.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showTypeDropdown) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = state.showTypeDropdown,
                    onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.ShowTypeDropdown(false)) }
                ) {
                    StadiumType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = { viewModel.handleEvent(AddPitchContract.Event.Type(type)) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Duration dropdown
            ExposedDropdownMenuBox(
                expanded = state.showDurationDropdown,
                onExpandedChange = { viewModel.handleEvent(AddPitchContract.Event.ShowDurationDropdown(it)) }
            ) {
                OutlinedTextField(
                    value = state.duration.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Duration") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDurationDropdown) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = state.showDurationDropdown,
                    onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.ShowDurationDropdown(false)) }
                ) {
                    StadiumDuration.entries.forEach { dur ->
                        DropdownMenuItem(
                            text = { Text(dur.label) },
                            onClick = { viewModel.handleEvent(AddPitchContract.Event.Duration(dur)) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Capacity + Price
            Row {
                OutlinedTextField(
                    value = state.capacity,
                    onValueChange = { viewModel.handleEvent(AddPitchContract.Event.Capacity(it)) },
                    label = { Text("Capacity") },
                    modifier = Modifier.weight(1f).focusRequester(capacityFocus),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { priceFocus.requestFocus() })
                )
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.pricePerHour,
                    onValueChange = { viewModel.handleEvent(AddPitchContract.Event.PricePerHour(it)) },
                    label = { Text("Price/Hour") },
                    modifier = Modifier.weight(1f).focusRequester(priceFocus),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { imageFocus.requestFocus() })
                )
            }
            Spacer(Modifier.height(12.dp))

            // Image URL
            OutlinedTextField(
                value = state.imageUrl,
                onValueChange = { viewModel.handleEvent(AddPitchContract.Event.ImageUrl(it)) },
                label = { Text("Image URL (optional)") },
                modifier = Modifier.fillMaxWidth().focusRequester(imageFocus),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            Spacer(Modifier.height(12.dp))

            // Time Selection
            Row {
                Button(onClick = { showOpenTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(state.openTime.ifEmpty { "Opening Time" })
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { showCloseTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(state.closeTime.ifEmpty { "Closing Time" })
                }
            }
            Spacer(Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = { viewModel.handleEvent(AddPitchContract.Event.Save) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.width(24.dp).height(24.dp))
                } else {
                    Text("Save Pitch")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
