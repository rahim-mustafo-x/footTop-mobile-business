package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.coder.foottopbusiness.core.ui.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPitchScreen(viewModel: AddPitchViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddPitchContract.Effect.NavigateBack -> onBack()
            }
        }
    }

    // Time picker dialog state
    var timePickerTarget by remember { mutableStateOf<Pair<Int, Boolean>?>(null) } // frameId to isStart

    if (timePickerTarget != null) {
        val (frameId, isStart) = timePickerTarget!!
        val pickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { timePickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    val time = "${pickerState.hour.toString().padStart(2, '0')}:${pickerState.minute.toString().padStart(2, '0')}"
                    if (isStart)
                        viewModel.handleEvent(AddPitchContract.Event.UpdateStartTime(state.selectedDay, frameId, time))
                    else
                        viewModel.handleEvent(AddPitchContract.Event.UpdateEndTime(state.selectedDay, frameId, time))
                    timePickerTarget = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { timePickerTarget = null }) { Text("Cancel") }
            },
            text = { TimePicker(state = pickerState) }
        )
    }

    // Copy dialog
    if (state.showCopyDialog) {
        val daysWithFrames = WeekDay.values()
            .filter { it != state.selectedDay && (state.schedules[it]?.isNotEmpty() == true || true) }
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(AddPitchContract.Event.DismissCopyDialog) },
            title = { Text("Copy times to") },
            text = {
                Column {
                    daysWithFrames.forEach { day ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.handleEvent(AddPitchContract.Event.ToggleCopyDay(day)) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = day in state.copyTargetDays,
                                onCheckedChange = { viewModel.handleEvent(AddPitchContract.Event.ToggleCopyDay(day)) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(day.label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.handleEvent(AddPitchContract.Event.ConfirmCopy) }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(AddPitchContract.Event.DismissCopyDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }

    val isFormValid = state.pitchName.isNotBlank()
    val days = WeekDay.values()
    val selectedIndex = days.indexOf(state.selectedDay)

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
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
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.pitchName,
                    onValueChange = { viewModel.handleEvent(AddPitchContract.Event.PitchName(it)) },
                    label = { Text("Pitch Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Text("Prices & Times", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }

            // Day tabs
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 16.dp,
                indicator = {},
                divider = {}
            ) {
                days.forEachIndexed { index, day ->
                    val selected = index == selectedIndex
                    Tab(
                        selected = selected,
                        onClick = { viewModel.handleEvent(AddPitchContract.Event.SelectDay(day)) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) Primary else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.short,
                                fontSize = 12.sp,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            val currentFrames = state.schedules[state.selectedDay] ?: emptyList()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Timeframes
                currentFrames.forEach { frame ->
                    TimeFrameRow(
                        frame = frame,
                        onStartClick = { timePickerTarget = Pair(frame.id, true) },
                        onEndClick = { timePickerTarget = Pair(frame.id, false) },
                        onPriceChange = { viewModel.handleEvent(AddPitchContract.Event.UpdatePrice(state.selectedDay, frame.id, it)) },
                        onDelete = { viewModel.handleEvent(AddPitchContract.Event.RemoveTimeFrame(state.selectedDay, frame.id)) }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Add timeframe row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Time Frame", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { viewModel.handleEvent(AddPitchContract.Event.AddTimeFrame(state.selectedDay)) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Primary
                        )
                    }
                }

                if (currentFrames.isNotEmpty()) {
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = { viewModel.handleEvent(AddPitchContract.Event.ShowCopyDialog) }
                    ) {
                        Text("Copy times to", color = Primary, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.handleEvent(AddPitchContract.Event.Save) },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Save", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFrameRow(
    frame: TimeFrame,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onPriceChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Time Frame", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeChip(label = "Start", value = frame.startTime, modifier = Modifier.weight(1f), onClick = onStartClick)
            TimeChip(label = "End", value = frame.endTime, modifier = Modifier.weight(1f), onClick = onEndClick)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = frame.price,
            onValueChange = onPriceChange,
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun TimeChip(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value.ifBlank { "--:--" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
