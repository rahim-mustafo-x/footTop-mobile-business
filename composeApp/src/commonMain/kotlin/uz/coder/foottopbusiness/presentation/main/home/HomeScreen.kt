@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.settings.SettingsVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchVoyager
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsVoyager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    
    // Screens overlay
    state.selectedStadiumForTime?.let {
        SlotsControlScreen(it, state, viewModel)
        return
    }
    
    state.selectedTournament?.let { t ->
        TournamentDetailScreen(t, onBack = { viewModel.handleEvent(HomeContract.Event.ClearTournament) })
        return
    }

    Scaffold(
        bottomBar = { MalaebBottomBar(state.currentTab) { viewModel.handleEvent(HomeContract.Event.ChangeTab(it)) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding(), start = padding.calculateStartPadding(LayoutDirection.Ltr), end = padding.calculateEndPadding(
                    LayoutDirection.Ltr))
                .background(Color(0xFFF8F9FA))
        ) {
            when (state.currentTab) {
                0 -> HomeTab(state, viewModel, onAddStadium = { navigator.push(AddPitchVoyager) }, onAddTournament = { navigator.push(TournamentsVoyager) })
                1 -> HistoryTab(state)
                2 -> {
                    LaunchedEffect(Unit) {
                        navigator.push(SettingsVoyager)
                        viewModel.handleEvent(HomeContract.Event.ChangeTab(0))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTab(state: HomeContract.State, viewModel: HomeViewModel, onAddStadium: () -> Unit, onAddTournament: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        MalaebHeader(state) { viewModel.handleEvent(HomeContract.Event.ChangeTab(2)) }
        
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
                    ActionCard("Stadion Qo'shish", Icons.Default.AddCircle, Color(0xFF4CAF50), Modifier.weight(1f), onClick = onAddStadium)
                    ActionCard("Turnir Ochish", Icons.Default.EmojiEvents, Color(0xFFFF9800), Modifier.weight(1f), onClick = onAddTournament)
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mening Stadionlarim", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = {}) { Text("Hammasi", color = Primary) }
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bugungi Daromad", color = Color.Gray, fontSize = 12.sp)
                        Text("${state.totalEarnings.toInt()} so'm", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                    }
                    Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF4CAF50))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MalaebDashboard(state: HomeContract.State, viewModel: HomeViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallStatCard("Faol", state.activeStadiums.toString(), Icons.Default.Stadium, Color(0xFF2196F3), Modifier.weight(1f).clickable{
            viewModel.handleEvent(HomeContract.Event.Stadium)
        })
        SmallStatCard("O'yinlar", state.totalMatches.toString(), Icons.Default.SportsSoccer, Color(0xFF9C27B0), Modifier.weight(1f).clickable{
            viewModel.handleEvent(HomeContract.Event.Match)
        })
        SmallStatCard("Turnirlar", state.totalTournaments.toString(), Icons.Default.EmojiEvents, Color(0xFFFF9800), Modifier.weight(1f).clickable{
            viewModel.handleEvent(HomeContract.Event.Tournament)
        })
    }
}

@Composable
private fun SmallStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun ActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Stadium, null, tint = Primary, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stadium.name ?: "Stadion", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${stadium.districtName}, ${stadium.regionName}", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(" ${stadium.openTime?.take(5)}-${stadium.closeTime?.take(5)}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotsControlScreen(stadium: StadiumResponse, state: HomeContract.State, viewModel: HomeViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stadium.name ?: "Slotlar") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleEvent(HomeContract.Event.ClearStadiumForSlots) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listOf("2024-05-20", "2024-05-21", "2024-05-22", "2024-05-23", "2024-05-24")) { date ->
                    val isSelected = state.selectedDate == date
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Primary else Color.White)
                            .border(1.dp, if (isSelected) Primary else Color.LightGray, RoundedCornerShape(12.dp))
                            .clickable { viewModel.handleEvent(HomeContract.Event.ChangeDate(date)) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(date.takeLast(2), color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Vaqt Slotlari", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            
            if (state.isLoadingSlots) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.stadiumSlots) { slot ->
                        val isAvailable = slot.available == true
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                .border(1.dp, if (isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(slot.start?.take(5) ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(if (isAvailable) "Bo'sh" else "Band", fontSize = 10.sp, color = if (isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MalaebBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = selectedTab == 0, onClick = { onTabSelected(0) }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Asosiy") })
        NavigationBarItem(selected = selectedTab == 1, onClick = { onTabSelected(1) }, icon = { Icon(Icons.Default.History, null) }, label = { Text("Tarix") })
        NavigationBarItem(selected = selectedTab == 2, onClick = { onTabSelected(2) }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profil") })
    }
}

@Composable
private fun MatchRow(m: MatchResponseDto, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SportsSoccer, null, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(m.title ?: "—", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Primary)
                Text("${m.currentPlayers}/${m.maxPlayers} o'yinchi", fontSize = 12.sp, color = Color.Gray)
            }
            Text("${m.pricePerPlayer?.toInt()} so'm", fontWeight = FontWeight.Bold, color = Primary)
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
            DetailRow("Sana", "${t.startDate} - ${t.endDate}")
            DetailRow("Mukofot", t.prizes ?: "Mavjud emas")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
}
