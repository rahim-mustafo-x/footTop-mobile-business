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
        val userState by koinInject<uz.coder.foottopbusiness.presentation.main.settings.SettingsViewModel>().state.collectAsState()
        val isAdmin = userState.user?.roles?.any { it.name == "ROLE_ADMIN" } ?: false

        var userId by remember { mutableStateOf("") }
        var specialty by remember { mutableStateOf("MURABBIY") }
        var expYears by remember { mutableStateOf("") }
        var hourlyRate by remember { mutableStateOf("") }
        var availability by remember { mutableStateOf("") }

        val specialties = if (isAdmin) listOf("MURABBIY", "ADMIN", "EGASI") else listOf("MURABBIY")
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
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(top = statusBarPadding + 16.dp, start = 8.dp, end = 24.dp, bottom = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(
                            "Coach qo'shish",
                            color = MaterialTheme.colorScheme.onPrimary,
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
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Murabbiy ma'lumotlari",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                CoachInputField(
                    value = userId,
                    onValueChange = { userId = it.filter { c -> c.isDigit() } },
                    label = "Foydalanuvchi ID",
                    icon = Icons.Default.Person,
                    keyboardType = KeyboardType.Number
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mutaxassislik", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isAdmin) {
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
                                leadingIcon = { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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
                    } else {
                        OutlinedTextField(
                            value = specialty,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            enabled = false
                        )
                    }
                }

                CoachInputField(
                    value = expYears,
                    onValueChange = { expYears = it.filter { c -> c.isDigit() } },
                    label = "Tajriba (yil)",
                    placeholder = "Masalan: 5",
                    icon = Icons.Default.History,
                    keyboardType = KeyboardType.Number
                )

                CoachInputField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Soatlik narx (so'm)",
                    placeholder = "Masalan: 150000",
                    icon = Icons.Default.Money,
                    keyboardType = KeyboardType.Decimal
                )

                CoachInputField(
                    value = availability,
                    onValueChange = { availability = it },
                    label = "Mavjudlik (ixtiyoriy)",
                    placeholder = "Masalan: Dushanba-Juma, 18:00-21:00",
                    icon = Icons.Default.Schedule
                )

                if (state.error != null) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !state.isCreating && userId.isNotBlank() && specialty.isNotBlank()
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Saqlash", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
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
        placeholder: String = "",
        icon: ImageVector,
        keyboardType: KeyboardType = KeyboardType.Text
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
