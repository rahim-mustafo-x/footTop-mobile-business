@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
            title = { Text("O'chirishni tasdiqlang", color = Primary) },
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
        CenterAlignedTopAppBar(
            title = { Text("FootTop Business", color = Primary, fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { viewModel.handleEvent(HomeContract.Event.Refresh) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
                }
            }
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Primary,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Primary,
                    height = 3.dp,
                )
            },
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
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> StadiumsTab(state, viewModel)
                1 -> TournamentsTab(state, viewModel)
                2 -> MatchesTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun StadiumsTab(state: HomeContract.State, viewModel: HomeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.handleEvent(HomeContract.Event.Search(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Qidiruv...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
            )
        }

        if (state.isLoadingStadiums && state.stadiums.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val scrollState = rememberScrollState()
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.horizontalScroll(scrollState)) {
                    // Header
                    Row(
                        modifier = Modifier.background(Primary.copy(alpha = 0.1f)).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Nomi", "Narxi", "Vaqti", "Holat", "Amal").forEach { header ->
                            Text(
                                text = header,
                                modifier = Modifier.width(100.dp).padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Rows
                    if (state.stadiums.isEmpty()) {
                        Text(
                            "Ma'lumot topilmadi",
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.stadiums.forEach { item ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(item.name ?: "—")
                                TableCell("${item.pricePerHour?.toInt() ?: 0} so'm")
                                TableCell("${item.openTime?.take(5) ?: "—"}-${item.closeTime?.take(5) ?: "—"}")
                                TableCell(
                                    text = if (item.isActive == true) "Faol" else "Nofaol",
                                    color = if (item.isActive == true) Color(0xFF4CAF50) else Color.Gray
                                )
                                Box(Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { item.id?.let { viewModel.handleEvent(HomeContract.Event.DeleteRequest(it)) } }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text = text,
        modifier = Modifier.width(100.dp).padding(horizontal = 8.dp),
        fontSize = 12.sp,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TournamentsTab(state: HomeContract.State, viewModel: HomeViewModel) {
    if (state.isLoadingTournaments) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.tournaments) { t ->
                TournamentRow(t, onClick = { viewModel.handleEvent(HomeContract.Event.SelectTournament(t)) })
            }
        }
    }
}

@Composable
private fun MatchesTab(state: HomeContract.State, viewModel: HomeViewModel) {
    if (state.isLoadingMatches) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.matches) { m ->
                MatchRow(m, onClick = { viewModel.handleEvent(HomeContract.Event.SelectMatch(m)) })
            }
        }
    }
}

@Composable
private fun TournamentRow(t: TournamentResponseDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, null, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(t.name ?: "—", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Primary)
                Text("${t.startDate ?: "—"} – ${t.endDate ?: "—"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusBadge(t.status)
        }
    }
}

@Composable
private fun MatchRow(m: MatchResponseDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SportsSoccer, null, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(m.title ?: "—", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Primary)
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
        TopAppBar(
            title = { Text(t.name ?: "Turnir", color = Primary, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Primary) } }
        )
    }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            DetailRow("Sana", "${t.startDate} - ${t.endDate}")
            DetailRow("Mukofot", t.prizes ?: "Mavjud emas")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchDetailScreen(m: MatchResponseDto, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(m.title ?: "O'yin", color = Primary, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Primary) } }
        )
    }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            DetailRow("Vaqt", m.dateTime ?: "")
            DetailRow("Narx", "${m.pricePerPlayer} so'm")
        }
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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
