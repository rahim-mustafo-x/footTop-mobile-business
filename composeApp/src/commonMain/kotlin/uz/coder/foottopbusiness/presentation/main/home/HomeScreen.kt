package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.formatAsDate
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.settings.SettingsVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchVoyager
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsVoyager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSlotsControl: (StadiumResponse) -> Unit,
    navigateToStadiums: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    // Navigation trigger for Slots Control
    LaunchedEffect(state.selectedStadiumForTime) {
        state.selectedStadiumForTime?.let {
            navigateToSlotsControl(it)
        }
    }

    // Screens overlay for Tournament Detail (if kept as overlay)
    state.selectedTournament?.let { t ->
        TournamentDetailScreen(t, onBack = { viewModel.handleEvent(HomeContract.Event.ClearTournament) })
        return
    }

    Scaffold(
        bottomBar = {
            MalaebBottomBar(state.currentTab) {
                viewModel.handleEvent(HomeContract.Event.ChangeTab(it))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = padding.calculateBottomPadding(),
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            when (state.currentTab) {
                0 -> HomeTab(
                    state = state,
                    viewModel = viewModel,
                    onAddStadium = { navigator.push(AddPitchVoyager) },
                    onAddTournament = { navigator.push(TournamentsVoyager) },
                    navigateToStadiums = navigateToStadiums,
                    navigateToProfile = {
                        navigator.push(SettingsVoyager)
                    }
                )

                1 -> HistoryTab(state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTab(
    state: HomeContract.State,
    viewModel: HomeViewModel,
    onAddStadium: () -> Unit,
    onAddTournament: () -> Unit,
    navigateToStadiums: () -> Unit,
    navigateToProfile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MalaebHeader(state) { navigateToProfile() }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { MalaebDashboard(state, viewModel) }

            item {
                Text("Tezkor boshqaruv", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        "Stadion Qo'shish",
                        Icons.Default.AddCircle,
                        Color(0xFF4CAF50),
                        Modifier.weight(1f),
                        onClick = onAddStadium
                    )
                    ActionCard(
                        "Turnir Ochish",
                        Icons.Default.EmojiEvents,
                        Color(0xFFFF9800),
                        Modifier.weight(1f),
                        onClick = onAddTournament
                    )
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mening Stadionlarim", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = navigateToStadiums) { Text("Hammasi", color = Primary) }
                }
            }

            items(state.stadiums) { stadium ->
                MalaebStadiumCard(stadium) {
                    viewModel.handleEvent(HomeContract.Event.SelectStadiumForSlots(stadium))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTab(state: HomeContract.State) {
    Column(modifier = Modifier.fillMaxSize()) {

        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text("Hammasi") })
            FilterChip(selected = false, onClick = {}, label = { Text("O'yinlar") })
            FilterChip(selected = false, onClick = {}, label = { Text("Turnirlar") })
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Yaqindagi o'yinlar", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            items(state.matches) { match ->
                MatchRow(match) {}
            }
        }
    }
}

@Composable
private fun MalaebHeader(state: HomeContract.State, onProfileClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(Primary, Color(0xFF1B5E20))))
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Xush kelibsiz,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(state.user?.fullName ?: "Admin", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White)
                }
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bugungi Daromad", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("${state.totalEarnings.toInt()} so'm", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                    }
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF4CAF50))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MalaebDashboard(state: HomeContract.State, viewModel: HomeViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallStatCard("Faol", state.activeStadiums.toString(), Icons.Default.Stadium, Color(0xFF2196F3), Modifier.weight(1f).clickable {
            viewModel.handleEvent(HomeContract.Event.Stadium)
        })
        SmallStatCard("O'yinlar", state.totalMatches.toString(), Icons.Default.SportsSoccer, Color(0xFF9C27B0), Modifier.weight(1f).clickable {
            viewModel.handleEvent(HomeContract.Event.Match)
        })
        SmallStatCard("Turnirlar", state.totalTournaments.toString(), Icons.Default.EmojiEvents, Color(0xFFFF9800), Modifier.weight(1f).clickable {
            viewModel.handleEvent(HomeContract.Event.Tournament)
        })
    }
}

@Composable
private fun SmallStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color)
            Spacer(Modifier.height(8.dp))
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MalaebStadiumCard(stadium: StadiumResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Stadium, null, tint = Primary, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stadium.name ?: "Noma'lum stadion", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${stadium.districtName}, ${stadium.regionName}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(Icons.Default.Stadium, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun MatchRow(match: MatchResponseDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Match #${match.id}", fontWeight = FontWeight.Bold)
                Text(match.dateTime?.let { LocalDateTime.parse(it) }?.formatAsDate() ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(match.dateTime?.let { LocalDateTime.parse(it) }?.formatAsTime() ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Text("${match.pricePerPlayer?.toInt()} so'm", color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MalaebBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Bosh sahifa") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.History, null) },
            label = { Text("Tarix") }
        )
    }
}

@Composable
private fun TournamentDetailScreen(tournament: TournamentResponseDto, onBack: () -> Unit) {
    // Basic overlay for tournament details
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        Text("Turnir Tafsilotlari", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        Text("Nomi: ${tournament.name}")
        // More details...
    }
}
