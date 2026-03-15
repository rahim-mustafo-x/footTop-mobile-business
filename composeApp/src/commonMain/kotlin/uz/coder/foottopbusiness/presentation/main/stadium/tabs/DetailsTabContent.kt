package uz.coder.foottopbusiness.presentation.main.stadium.tabs

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumContract
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumDuration
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTabContent(state: StadiumContract.State, onEvent: (StadiumContract.Event) -> Unit) {
    val isFormValid = state.stadiumName.isNotBlank() && state.pricePerHour.isNotBlank()

    var showOpeningDialog by remember { mutableStateOf(false) }
    var showClosingDialog by remember { mutableStateOf(false) }

    if (showOpeningDialog) {
        TimePickerDialog(
            onDismiss = { showOpeningDialog = !showOpeningDialog },
            onConfirm = { hour, minute ->
                onEvent(StadiumContract.Event.OpeningTime("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"))
                showOpeningDialog = !showOpeningDialog
            }
        )
    }

    if (showClosingDialog) {
        TimePickerDialog(
            onDismiss = { showClosingDialog = !showClosingDialog },
            onConfirm = { hour, minute ->
                onEvent(StadiumContract.Event.ClosingTime("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"))
                showClosingDialog = !showClosingDialog
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.stadiumName,
                onValueChange = { onEvent(StadiumContract.Event.StadiumName(it)) },
                label = { Text("Stadium Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(StadiumContract.Event.Description(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                minLines = 2,
                maxLines = 4
            )
            Spacer(Modifier.height(12.dp))

            // Type dropdown
            ExposedDropdownMenuBox(
                expanded = state.showTypeDropdown,
                onExpandedChange = { onEvent(StadiumContract.Event.ShowTypeDropdown(it)) }
            ) {
                OutlinedTextField(
                    value = state.type.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showTypeDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = state.showTypeDropdown,
                    onDismissRequest = { onEvent(StadiumContract.Event.ShowTypeDropdown(false)) }
                ) {
                    StadiumType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = { onEvent(StadiumContract.Event.Type(type)) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Duration dropdown
            ExposedDropdownMenuBox(
                expanded = state.showDurationDropdown,
                onExpandedChange = { onEvent(StadiumContract.Event.ShowDurationDropdown(it)) }
            ) {
                OutlinedTextField(
                    value = state.duration.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Duration") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDurationDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = state.showDurationDropdown,
                    onDismissRequest = { onEvent(StadiumContract.Event.ShowDurationDropdown(false)) }
                ) {
                    StadiumDuration.entries.forEach { dur ->
                        DropdownMenuItem(
                            text = { Text(dur.label) },
                            onClick = { onEvent(StadiumContract.Event.Duration(dur)) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Row {
                OutlinedTextField(
                    value = state.capacity,
                    onValueChange = { onEvent(StadiumContract.Event.Capacity(it)) },
                    label = { Text("Capacity") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.pricePerHour,
                    onValueChange = { onEvent(StadiumContract.Event.PricePerHour(it)) },
                    label = { Text("Price/Hour") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimePickerField("Opening Time", state.openingTime, Modifier.weight(1f)) {
                    showOpeningDialog = true
                }
                TimePickerField("Closing Time", state.closingTime, Modifier.weight(1f)) {
                    showClosingDialog = true
                }
            }
            Spacer(Modifier.height(20.dp))

            SwitchRow("Upfront Enabled", state.upfrontEnabled) { onEvent(StadiumContract.Event.UpfrontEnabled(it)) }
            SwitchRow("Split Payment Enabled", state.splitPaymentEnabled) { onEvent(StadiumContract.Event.SplitPaymentEnabled(it)) }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { onEvent(StadiumContract.Event.Save) },
            enabled = isFormValid && !state.isLoading,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: (hour: Int, minute: Int) -> Unit) {
    val timePickerState = rememberTimePickerState(is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = timePickerState) }
    )
}

@Composable
fun TimePickerField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value.ifBlank { "Select Time" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
