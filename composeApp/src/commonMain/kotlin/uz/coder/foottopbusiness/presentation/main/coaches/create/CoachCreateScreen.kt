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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.border
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
        val isAdmin = userState.user?.roles?.any { it.name == "ROLE_ADMIN" || it.name == "ROLE_SUPER_ADMIN" } ?: false

        var userId by remember { mutableStateOf("") }
        var specialty by remember { mutableStateOf("") }
        var expYears by remember { mutableStateOf("") }
        var hourlyRate by remember { mutableStateOf("") }
        var availability by remember { mutableStateOf("") }

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
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(top = statusBarPadding, start = 8.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(
                            "Foydalanuvchi qo'shish",
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
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Foydalanuvchi ma'lumotlari",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        CoachInputField(
                            value = userId,
                            onValueChange = { userId = it.filter { c -> c.isDigit() } },
                            label = "Foydalanuvchi ID",
                            placeholder = "Masalan: 9",
                            icon = Icons.Default.Person,
                            keyboardType = KeyboardType.Number
                        )

                        CoachInputField(
                            value = specialty,
                            onValueChange = { specialty = it },
                            label = "Mutaxassislik (Coach)",
                            placeholder = "Masalan: Futbol",
                            icon = Icons.Default.Badge
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Professional ma'lumotlar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CoachInputField(
                                value = expYears,
                                onValueChange = { expYears = it.filter { c -> c.isDigit() } },
                                label = "Tajriba (yil)",
                                placeholder = "5",
                                icon = Icons.Default.History,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )

                            CoachInputField(
                                value = hourlyRate,
                                onValueChange = { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } },
                                label = "Narx (so'm)",
                                placeholder = "150 000",
                                icon = Icons.Default.Payments,
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CoachInputField(
                                value = availability,
                                onValueChange = { availability = it },
                                label = "Mavjudlik",
                                placeholder = "Masalan: Dushanba-Juma, 18:00-21:00",
                                icon = Icons.Default.Schedule
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Har kuni", "Dush-Jum", "Hafta oxiri").forEach { suggestion ->
                                    FilterChip(
                                        selected = availability == suggestion,
                                        onClick = { availability = suggestion },
                                        label = { Text(suggestion, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            state.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

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
                        .height(64.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !state.isCreating && userId.isNotBlank() && specialty.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("SAQLASH", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
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
        modifier: Modifier = Modifier,
        keyboardType: KeyboardType = KeyboardType.Text
    ) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            )
        }
    }
}
