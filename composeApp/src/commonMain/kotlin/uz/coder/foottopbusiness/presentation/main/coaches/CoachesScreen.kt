package uz.coder.foottopbusiness.presentation.main.coaches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto

@Composable
fun CoachesScreen(viewModel: CoachesViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CoachesContract.Effect.ShowToast -> { /* snackbar */ }
            }
        }
    }

    if (state.selectedCoach != null) {
        CoachDetailScreen(
            coach = state.selectedCoach!!,
            onBack = { viewModel.handleEvent(CoachesContract.Event.ClearDetail) }
        )
        return
    }

    if (state.showCreateDialog) {
        CreateCoachDialog(
            onDismiss = { viewModel.handleEvent(CoachesContract.Event.HideCreateDialog) },
            onCreate = { userId, specialty, expYears, rate, availability ->
                viewModel.handleEvent(CoachesContract.Event.Create(userId, specialty, expYears, rate, availability))
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.handleEvent(CoachesContract.Event.ShowCreateDialog) }, containerColor = Primary) {
                Icon(Icons.Default.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading || state.isCreating -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { viewModel.handleEvent(CoachesContract.Event.Load) }) { Text("Qayta urinish") }
                    }
                }
                state.coaches.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        Text("Murabbiylar yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.coaches, key = { it.id ?: 0L }) { coach ->
                        CoachCard(coach = coach, onClick = { viewModel.handleEvent(CoachesContract.Event.SelectCoach(coach)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateCoachDialog(
    onDismiss: () -> Unit,
    onCreate: (userId: Long, specialty: String, experienceYears: Int, hourlyRate: Double, availability: String?) -> Unit,
) {
    var userId by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var expYears by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi murabbiy", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = userId, onValueChange = { userId = it.filter { c -> c.isDigit() } },
                    label = { Text("Foydalanuvchi ID") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = specialty, onValueChange = { specialty = it },
                    label = { Text("Mutaxassislik") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expYears, onValueChange = { expYears = it.filter { c -> c.isDigit() } },
                    label = { Text("Tajriba (yil)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = hourlyRate, onValueChange = { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Soatlik narx (so'm)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = availability, onValueChange = { availability = it },
                    label = { Text("Mavjudlik (ixtiyoriy)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userId.isNotBlank() && specialty.isNotBlank()) {
                        onCreate(userId.toLongOrNull() ?: 0L, specialty,
                            expYears.toIntOrNull() ?: 0, hourlyRate.toDoubleOrNull() ?: 0.0,
                            availability.takeIf { it.isNotBlank() })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Qo'shish") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Bekor qilish") } }
    )
}

@Composable
private fun CoachCard(coach: CoachResponseDto, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = Primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(coach.coachName ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(coach.specialty ?: "—", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${coach.hourlyRate?.toInt() ?: 0} so'm/soat", fontSize = 12.sp, color = Primary)
                Text("${coach.experienceYears ?: 0} yil", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoachDetailScreen(coach: CoachResponseDto, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(coach.coachName ?: "Murabbiy") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(56.dp), tint = Primary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(coach.coachName ?: "—", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(coach.specialty ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            DetailRow("Tajriba", "${coach.experienceYears ?: 0} yil")
            DetailRow("Narx", "${coach.hourlyRate?.toInt() ?: 0} so'm/soat")
            DetailRow("Mavjudlik", coach.availability ?: "—")
            if (!coach.reviews.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Sharhlar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(coach.reviews, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
    HorizontalDivider()
}
