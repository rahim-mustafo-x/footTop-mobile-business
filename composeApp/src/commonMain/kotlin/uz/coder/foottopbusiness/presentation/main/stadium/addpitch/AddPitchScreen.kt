package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import androidx.compose.foundation.background
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
import uz.coder.foottopbusiness.core.ui.Primary

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
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF0F3D2E))
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
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Yangi stadion",
                        color = Color.White,
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
                "${state.selectedRegion?.name ?: "Toshkent"}, ${state.selectedDistrict?.name ?: "Yunusobod"}",
                color = Color.Gray,
                fontSize = 16.sp
            )

            LabelAndField("ANIQ MANZIL", state.name, "Ko'cha, uy raqami") {
                viewModel.handleEvent(AddPitchContract.Event.Name(it))
            }

            LabelAndField("MAYDONLAR SONI", state.capacity, "3", KeyboardType.Number) {
                viewModel.handleEvent(AddPitchContract.Event.Capacity(it))
            }

            LabelAndField("SPORT TURI", "Futbol", "Futbol") {
                // TODO: Update sport type
            }

            LabelAndField("SOATLIK NARX (SO'M)", state.pricePerHour, "50 000", KeyboardType.Number) {
                viewModel.handleEvent(AddPitchContract.Event.PricePerHour(it))
            }

            Column {
                Text("ISH VAQTI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(state.openTime, fontSize = 16.sp, modifier = Modifier.clickable { showOpenTimePicker = true })
                    Text("          ", fontSize = 16.sp)
                    Text(state.closeTime, fontSize = 16.sp, modifier = Modifier.clickable { showCloseTimePicker = true })
                }
            }

            Column {
                Text("RASM YUKLASH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { /* TODO: Image picker */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D2E))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
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
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
            )
        )
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
