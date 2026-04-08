package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.home.history.HistoryScreen
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

    LaunchedEffect(state.selectedStadiumForTime) {
        state.selectedStadiumForTime?.let {
            navigateToSlotsControl(it)
        }
    }

    state.selectedTournament?.let { t ->
        TournamentDetailScreen(t, onBack = { viewModel.handleEvent(HomeContract.Event.ClearTournament) })
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.currentTab) {
                0 -> HomeTab(
                    state = state,
                    viewModel = viewModel,
                    onAddStadium = { navigator.push(AddPitchVoyager) },
                    onAddTournament = { navigator.push(TournamentsVoyager) },
                    navigateToStadiums = navigateToStadiums,
                    onProfileClick = { navigator.push(SettingsVoyager) }
                )
                1 -> HistoryScreen(state)
            }
        }
    }
}

@Composable
private fun HomeTab(
    state: HomeContract.State,
    viewModel: HomeViewModel,
    onAddStadium: () -> Unit,
    onAddTournament: () -> Unit,
    navigateToStadiums: () -> Unit,
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            MalaebHeader(state, onProfileClick)
        }

        item {
            MalaebDashboard(state, viewModel, Modifier.padding(horizontal = 16.dp))
        }

        item {
            Text(
                "Tezkor boshqaruv", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mening Stadionlarim", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = navigateToStadiums) { Text("Hammasi", color = Primary) }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.stadiums.take(4)) { stadium ->
                    MalaebStadiumCard(
                        stadium = stadium,
                        modifier = Modifier.width(280.dp)
                    ) {
                        viewModel.handleEvent(HomeContract.Event.SelectStadiumForSlots(stadium))
                    }
                }
            }
        }
    }
}

@Composable
private fun MalaebHeader(state: HomeContract.State, onProfileClick: () -> Unit) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(Primary, Color(0xFF1B5E20))))
            .padding(top = statusBarPadding + 16.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Xush kelibsiz,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(state.user?.fullName ?: "Admin", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        }
    }
}

@Composable
private fun MalaebDashboard(state: HomeContract.State, viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallStatCard("Faol", state.activeStadiums.toString(), Icons.Default.SportsSoccer, Color(0xFF2196F3), Modifier.weight(1f).clickable {
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MalaebStadiumCard(stadium: StadiumResponse, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SportsSoccer, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stadium.name ?: "Noma'lum stadion", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${stadium.districtName}, ${stadium.regionName}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun TournamentDetailScreen(tournament: TournamentResponseDto, onBack: () -> Unit) {
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
    }
}
