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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.presentation.main.tournaments.create.TournamentCreateScreen
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsScreen(viewModel: TournamentsViewModel) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(Unit) {
        viewModel.handleEvent(TournamentsContract.Event.Load)
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

    Scaffold(
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = statusBarPadding + 16.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Turnirlar",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { navigator.push(TournamentCreateScreen()) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(
                    LayoutDirection.Rtl), end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Search and Filters
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Qidirish...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                val filterTabs = listOf("Barchasi", "Kutilmoqda", "Davom etmoqda", "Tugagan")
                var selectedFilterIndex by remember { mutableStateOf(0) }

                ScrollableTabRow(
                    selectedTabIndex = selectedFilterIndex,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    filterTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedFilterIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                title,
                                color = if (selectedFilterIndex == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            when {
                state.isLoading || state.isCreating -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Yaratish") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}

@Composable
private fun TournamentCard(t: TournamentResponseDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    t.name ?: "—",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${t.startDate ?: "—"} - ${t.endDate ?: "—"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status and Badge
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(t.status)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${t.teamApplied ?: 0}/${t.maxTeams ?: 0} jamoa",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
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
        Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(
            LayoutDirection.Ltr), end = padding.calculateEndPadding(LayoutDirection.Rtl)).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(tournament.name ?: "—", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("${tournament.startDate ?: "—"} – ${tournament.endDate ?: "—"}", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
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
        "UPCOMING" -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        "ONGOING" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        "FINISHED" -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurface
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
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
