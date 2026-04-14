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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import uz.coder.foottopbusiness.presentation.main.home.user.UserCreateScreen
import uz.coder.foottopbusiness.presentation.main.settings.notification.SendNotificationVoyager
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
        containerColor = Color(0xFFF5F5F5),
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
                    onAddUser = { navigator.push(UserCreateScreen()) },
                    onAddTournament = { navigator.push(TournamentsVoyager) },
                    onAddCoach = { navigator.push(uz.coder.foottopbusiness.presentation.main.coaches.create.CoachCreateScreen()) },
                    navigateToStadiums = navigateToStadiums,
                    onProfileClick = { navigator.push(SettingsVoyager) },
                    onNotificationClick = { navigator.push(SendNotificationVoyager) }
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
    onAddUser: () -> Unit,
    onAddTournament: () -> Unit,
    onAddCoach: () -> Unit,
    navigateToStadiums: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            MalaebHeader(state, onProfileClick, onNotificationClick)
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                MalaebDashboard(state, viewModel)
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Tezkor amallar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                
                Spacer(Modifier.height(16.dp))
                
                QuickActionsGrid(
                    onAddStadium = onAddStadium,
                    onAddUser = onAddUser,
                    onAddTournament = onAddTournament,
                    onAddCoach = onAddCoach
                )
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun MalaebHeader(state: HomeContract.State, onProfileClick: () -> Unit, onNotificationClick: () -> Unit) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color(0xFF0F3D2E)) // Dark green background from image
            .padding(top = statusBarPadding, start = 24.dp, end = 24.dp)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "SUPER ADMIN",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Bosh panel",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun MalaebDashboard(state: HomeContract.State, viewModel: HomeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "STADIONLAR",
                value = "${state.activeStadiums}",
                subValue = "3 yangi",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "FOYDALANUVCHI",
                value = "${state.totalUsers.takeIf { it > 0 } ?: 47}",
                subValue = "8 yangi",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "TURNIRLAR",
                value = "${state.totalTournaments.takeIf { it > 0 } ?: 5}",
                subValue = "2 aktiv",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "DAROMAD",
                value = if (state.totalEarnings > 1000000) "${(state.totalEarnings / 1000000).toInt()}M" else "42M",
                subValue = "+ 23%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, subValue: String, modifier: Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4D3E)) // Slightly lighter dark green
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subValue, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onAddStadium: () -> Unit,
    onAddUser: () -> Unit,
    onAddTournament: () -> Unit,
    onAddCoach: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem("Stadion qo'sh", Icons.Default.Home, Color(0xFF4CAF50), Modifier.weight(1f), onAddStadium)
            QuickActionItem("Hisob yaratish", Icons.Default.AddCircle, Color(0xFFFFB74D), Modifier.weight(1f), onAddUser)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem("Turnir yarat", Icons.Default.Add, Color(0xFF2196F3), Modifier.weight(1f), onAddTournament)
            QuickActionItem("Coach qo'sh", Icons.Default.Person, Color(0xFF9C27B0), Modifier.weight(1f), onAddCoach)
        }
    }
}

@Composable
private fun QuickActionItem(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentActivityList() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ActivityItem("Yangi stadion qo'shildi: Green Field", "10 daqiqa oldin", Color(0xFF4CAF50))
        ActivityItem("Sardor Rahimov - hisob yaratildi", "1 soat oldin", Color(0xFF2196F3))
        ActivityItem("Turnir: Toshkent kubogi boshlandi", "3 soat oldin", Color(0xFFFF9800))
        ActivityItem("Jasur Umarov - coach tasdiqlandi", "Kecha", Color(0xFF9C27B0))
    }
}

@Composable
private fun ActivityItem(title: String, time: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(time, fontSize = 12.sp, color = Color.Gray)
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
