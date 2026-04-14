package uz.coder.foottopbusiness.presentation.main.coaches.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesContract
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesViewModel

class CoachCreateScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<CoachesViewModel>()
        val state by viewModel.state.collectAsState()

        var userId by remember { mutableStateOf("") }
        var specialty by remember { mutableStateOf("MURABBIY") }
        var expYears by remember { mutableStateOf("") }
        var hourlyRate by remember { mutableStateOf("") }
        var availability by remember { mutableStateOf("") }

        val specialties = listOf("MURABBIY", "ADMIN", "EGASI")
        var expanded by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                if (effect is CoachesContract.Effect.ShowToast) {
                    if (effect.message == "Murabbiy qo'shildi") {
                        navigator.pop()
                    }
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(Color(0xFF0F3D2E))
                        .padding(top = statusBarPadding + 16.dp, start = 8.dp, end = 24.dp, bottom = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text(
                            "Coach qo'shish",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(
                        LayoutDirection.Ltr), end = padding.calculateEndPadding(LayoutDirection.Rtl))
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Murabbiy ma'lumotlari",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                CoachInputField(
                    value = userId,
                    onValueChange = { userId = it.filter { c -> c.isDigit() } },
                    label = "Foydalanuvchi ID",
                    icon = Icons.Default.Person,
                    keyboardType = KeyboardType.Number
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mutaxassislik", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = specialty,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = Color(0xFF0F3D2E)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0F3D2E),
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            specialties.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        specialty = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                CoachInputField(
                    value = expYears,
                    onValueChange = { expYears = it.filter { c -> c.isDigit() } },
                    label = "Tajriba (yil)",
                    icon = Icons.Default.History,
                    keyboardType = KeyboardType.Number
                )

                CoachInputField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Soatlik narx (so'm)",
                    icon = Icons.Default.Money,
                    keyboardType = KeyboardType.Decimal
                )

                CoachInputField(
                    value = availability,
                    onValueChange = { availability = it },
                    label = "Mavjudlik (ixtiyoriy)",
                    icon = Icons.Default.Schedule
                )

                if (state.error != null) {
                    Text(state.error!!, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.handleEvent(
                            CoachesContract.Event.Create(
                                userId.toLongOrNull() ?: 0L,
                                specialty,
                                expYears.toIntOrNull() ?: 0,
                                hourlyRate.toDoubleOrNull() ?: 0.0,
                                availability.takeIf { it.isNotBlank() }
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D2E)),
                    enabled = !state.isCreating && userId.isNotBlank() && specialty.isNotBlank()
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Saqlash", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    private fun CoachInputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        icon: ImageVector,
        keyboardType: KeyboardType = KeyboardType.Text
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(icon, null, tint = Color(0xFF0F3D2E)) },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F3D2E),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }
    }
}
