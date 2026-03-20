@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

private val HOME_TABS = listOf("Stadionlar", "Turnirlar", "O'yinlar")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Detail screens — full screen overlay
    state.selectedTournament?.let { t ->
        TournamentDetailScreen(t, onBack = { viewModel.handleEvent(HomeContract.Event.ClearTournament) })
        return
    }
    state.selectedMatch?.let { m ->
        MatchDetailScreen(m, onBack = { viewModel.handleEvent(HomeContract.Event.ClearMatch) })
        return
    }

    // Delete dialog
    if (state.deletingId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(HomeContract.Event.DeleteCancel) },
            title = { Text("O'chirishni tasdiqlang") },
            text = { Text("Bu stadionni o'chirishni xohlaysizmi?") },
            confirmButton = {
                TextButton(onClick = { viewModel.handleEvent(HomeContract.Event.DeleteConfirm) }) {
                    Text("O'chirish", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(HomeContract.Event.DeleteCancel) }) { Text("Bekor qilish") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(Primary).padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Bosh sahifa", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = { viewModel.handleEvent(HomeContract.Event.Refresh) }) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White)
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Primary
                )
            }
        ) {
            HOME_TABS.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> StadiumsTab(state, viewModel)
            1 -> TournamentsTab(state, viewModel)
            2 -> MatchesTab(state, viewModel)
        }
    }
}

@Composable
private fun StadiumsTab(state: HomeContract.State, viewModel: HomeViewModel) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }.collect { last ->
            val total = listState.layoutInfo.totalItemsCount
            if (last != null && last >= total - 3 && !state.isLastPage && !state.isLoadingStadiums)
                viewModel.handleEvent(HomeContract.Event.LoadNextPage)
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        // Search
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.handleEvent(HomeContract.Event.Search(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("Stadion qidiring...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        }

        // Filter chips
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.filterActive == true,
                        onClick = { viewModel.handleEvent(HomeContract.Event.FilterActive(if (state.filterActive == true) null else true)) },
                        label = { Text("Faol") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White)
                    )
                }
                item {
                    FilterChip(
                        selected = state.filterActive == false,
                        onClick = { viewModel.handleEvent(HomeContract.Event.FilterActive(if (state.filterActive == false) null else false)) },
                        label = { Text("Nofaol") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.error, selectedLabelColor = Color.White)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        if (state.isLoadingStadiums && state.stadiums.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        } else if (state.stadiums.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Place, null, modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        Text("Stadionlar topilmadi", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(state.stadiums, key = { it.id ?: 0 }) { stadium ->
                StadiumCard(
                    stadium = stadium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                    onDelete = { viewModel.handleEvent(HomeContract.Event.DeleteRequest(stadium.id ?: 0)) }
                )
            }
            if (state.isLoadingStadiums) {
                item { Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), color = Primary) } }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun TournamentsTab(state: HomeContract.State, viewModel: HomeViewModel) {
    when {
        state.isLoadingTournaments -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        state.tournaments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("Turnirlar yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.tournaments, key = { "t_${it.id}" }) { t ->
                TournamentRow(t, modifier = Modifier, onClick = { viewModel.handleEvent(HomeContract.Event.SelectTournament(t)) })
            }
        }
    }
}

@Composable
private fun MatchesTab(state: HomeContract.State, viewModel: HomeViewModel) {
    when {
        state.isLoadingMatches -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        state.matches.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, null, modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("O'yinlar yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.matches, key = { "m_${it.id}" }) { m ->
                MatchRow(m, modifier = Modifier, onClick = { viewModel.handleEvent(HomeContract.Event.SelectMatch(m)) })
            }
        }
    }
}

@Composable
private fun StadiumCard(stadium: StadiumResponse, modifier: Modifier, onDelete: () -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(
                if (stadium.isActive == true) Color(0xFF4CAF50) else Color(0xFFBDBDBD), RoundedCornerShape(50)))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stadium.name ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniChip(stadium.type ?: "—", Primary)
                    if (!stadium.regionName.isNullOrBlank()) MiniChip(stadium.regionName, MaterialTheme.colorScheme.secondary)
                }
                Text("${stadium.pricePerHour?.toInt() ?: 0} so'm/soat  •  ${stadium.openTime ?: "—"}–${stadium.closeTime ?: "—"}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TournamentRow(t: TournamentResponseDto, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, null, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(t.name ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${t.startDate ?: "—"} – ${t.endDate ?: "—"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusBadge(t.status)
        }
    }
}

@Composable
private fun MatchRow(m: MatchResponseDto, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(m.title ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${m.currentPlayers ?: 0}/${m.maxPlayers ?: 0} o'yinchi  •  ${m.pricePerPlayer?.toInt() ?: 0} so'm",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MatchStatusBadge(m.status)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentDetailScreen(t: TournamentResponseDto, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(t.name ?: "Turnir") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Primary), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(t.name ?: "—", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Text("${t.startDate ?: "—"} – ${t.endDate ?: "—"}", color = Color.White.copy(alpha = 0.85f))
                }
            }
            Spacer(Modifier.height(16.dp))
            DetailRow("Holat", when(t.status) { "UPCOMING" -> "Kutilmoqda"; "ONGOING" -> "Davom etmoqda"; "FINISHED" -> "Tugagan"; else -> t.status ?: "—" })
            DetailRow("Sport turi", t.sportType ?: "—")
            DetailRow("Jamoalar", "${t.teamApplied ?: 0} / ${t.maxTeams ?: 0}")
            DetailRow("Ishtirok to'lovi", "${t.entryFee?.toInt() ?: 0} so'm")
            if (!t.address.isNullOrBlank()) DetailRow("Manzil", t.address)
            if (!t.prizes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text("Mukofotlar", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(t.prizes, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (!t.rules.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text("Qoidalar", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(t.rules, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchDetailScreen(m: MatchResponseDto, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(m.title ?: "O'yin") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Primary), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(m.title ?: "—", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Text(m.dateTime ?: "—", color = Color.White.copy(alpha = 0.85f))
                }
            }
            Spacer(Modifier.height(16.dp))
            DetailRow("Holat", when(m.status) { "OPEN" -> "Ochiq"; "FULL" -> "To'lgan"; "CANCELLED" -> "Bekor"; else -> m.status ?: "—" })
            DetailRow("Sport turi", m.sportType ?: "—")
            DetailRow("O'yinchilar", "${m.currentPlayers ?: 0} / ${m.maxPlayers ?: 0}")
            DetailRow("Narx", "${m.pricePerPlayer?.toInt() ?: 0} so'm/o'yinchi")
            DetailRow("Davomiyligi", m.duration ?: "—")
        }
    }
}

@Composable
private fun MiniChip(text: String, color: Color) {
    Box(modifier = Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val (bg, label) = when (status) {
        "UPCOMING" -> Color(0xFF2196F3) to "Kutilmoqda"
        "ONGOING" -> Color(0xFF4CAF50) to "Davom etmoqda"
        "FINISHED" -> Color(0xFF9E9E9E) to "Tugagan"
        else -> MaterialTheme.colorScheme.surfaceVariant to (status ?: "—")
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
        Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MatchStatusBadge(status: String?) {
    val (bg, label) = when (status) {
        "OPEN" -> Color(0xFF4CAF50) to "Ochiq"
        "FULL" -> Color(0xFFFF9800) to "To'lgan"
        "CANCELLED" -> Color(0xFF9E9E9E) to "Bekor"
        else -> MaterialTheme.colorScheme.surfaceVariant to (status ?: "—")
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
        Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
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
