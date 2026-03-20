package uz.coder.foottopbusiness.presentation.main.tournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto

@Composable
fun TournamentsScreen(viewModel: TournamentsViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TournamentsContract.Effect.ShowToast -> { /* snackbar yoki toast */ }
            }
        }
    }

    if (state.selectedTournament != null) {
        TournamentDetailScreen(
            tournament = state.selectedTournament!!,
            onBack = { viewModel.handleEvent(TournamentsContract.Event.ClearDetail) }
        )
        return
    }

    if (state.showCreateDialog) {
        CreateTournamentDialog(
            onDismiss = { viewModel.handleEvent(TournamentsContract.Event.HideCreateDialog) },
            onCreate = { name, start, end, maxTeams, fee, address ->
                viewModel.handleEvent(TournamentsContract.Event.Create(name, start, end, maxTeams, fee, address))
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.handleEvent(TournamentsContract.Event.ShowCreateDialog) }, containerColor = Primary) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
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
                        TextButton(onClick = { viewModel.handleEvent(TournamentsContract.Event.Load) }) { Text("Qayta urinish") }
                    }
                }
                state.tournaments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        Text("Turnirlar yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.tournaments, key = { it.id ?: 0L }) { t ->
                        TournamentCard(t, onClick = { viewModel.handleEvent(TournamentsContract.Event.Select(t)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTournamentDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, startDate: String, endDate: String, maxTeams: Int, entryFee: Double, address: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var maxTeams by remember { mutableStateOf("") }
    var entryFee by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi turnir", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nomi") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startDate, onValueChange = { startDate = it },
                    label = { Text("Boshlanish sanasi (yyyy-MM-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = endDate, onValueChange = { endDate = it },
                    label = { Text("Tugash sanasi (yyyy-MM-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = maxTeams, onValueChange = { maxTeams = it.filter { c -> c.isDigit() } },
                    label = { Text("Maksimal jamoalar") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = entryFee, onValueChange = { entryFee = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Ishtirok to'lovi (so'm)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text("Manzil (ixtiyoriy)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()) {
                        onCreate(name, startDate, endDate, maxTeams.toIntOrNull() ?: 0,
                            entryFee.toDoubleOrNull() ?: 0.0, address.takeIf { it.isNotBlank() })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Yaratish") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}

@Composable
private fun TournamentCard(t: TournamentResponseDto, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(t.name ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("${t.startDate ?: "—"} – ${t.endDate ?: "—"}", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(t.status)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip("${t.teamApplied ?: 0}/${t.maxTeams ?: 0} jamoa")
                InfoChip("${t.entryFee?.toInt() ?: 0} so'm")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentDetailScreen(tournament: TournamentResponseDto, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(tournament.name ?: "Turnir") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(tournament.name ?: "—", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text("${tournament.startDate ?: "—"} – ${tournament.endDate ?: "—"}", color = Color.White.copy(alpha = 0.85f))
                }
            }
            Spacer(Modifier.height(16.dp))
            DetailRow("Holat", tournament.status ?: "—")
            DetailRow("Sport turi", tournament.sportType ?: "—")
            DetailRow("Jamoalar", "${tournament.teamApplied ?: 0} / ${tournament.maxTeams ?: 0}")
            DetailRow("Ishtirok to'lovi", "${tournament.entryFee?.toInt() ?: 0} so'm")
            if (!tournament.address.isNullOrBlank()) DetailRow("Manzil", tournament.address)
            if (!tournament.prizes.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Mukofotlar", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(tournament.prizes, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!tournament.rules.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Qoidalar", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(tournament.rules, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusChip(status: String?) {
    val (bg, fg) = when (status) {
        "UPCOMING" -> Color(0xFF2196F3) to Color.White
        "ONGOING" -> Color(0xFF4CAF50) to Color.White
        "FINISHED" -> Color(0xFF9E9E9E) to Color.White
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        "UPCOMING" -> "Kutilmoqda"; "ONGOING" -> "Davom etmoqda"; "FINISHED" -> "Tugagan"; else -> status ?: "—"
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(modifier = Modifier.background(Primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, fontSize = 12.sp, color = Primary)
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
