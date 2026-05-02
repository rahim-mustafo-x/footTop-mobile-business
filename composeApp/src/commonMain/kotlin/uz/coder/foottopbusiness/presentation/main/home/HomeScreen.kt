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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.platform.exitApp
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.home.history.HistoryScreen
import uz.coder.foottopbusiness.presentation.main.reports.ReportItem
import uz.coder.foottopbusiness.presentation.main.settings.SettingsVoyager
import uz.coder.foottopbusiness.presentation.main.settings.notification.SendNotificationVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchVoyager
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsVoyager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSlotsControl: (StadiumResponse) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = remember { SnackbarHostState() }
    var lastBackPressTime by remember { mutableStateOf(0L) }

    BackHandler(enabled = true) {
        when {
            state.selectedTournament != null -> viewModel.handleEvent(HomeContract.Event.ClearTournament)
            state.selectedMatch != null -> viewModel.handleEvent(HomeContract.Event.ClearMatch)
            state.selectedStadiumForTime != null -> viewModel.handleEvent(HomeContract.Event.ClearStadiumForSlots)
            state.currentTab != 0 -> viewModel.handleEvent(HomeContract.Event.ChangeTab(0))
            else -> {
                val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
                if (currentTime - lastBackPressTime < 2000) {
                    exitApp()
                } else {
                    lastBackPressTime = currentTime
                    viewModel.handleEvent(HomeContract.Event.ShowExitToast)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeContract.Effect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                else -> {}
            }
        }
    }

    // Placeholder for platform-specific permission check/request
    // In a real KMP app, you might use a library like MOKO Permissions 
    // or a custom platform-specific bridge.
    // For now, we'll simulate the logic as requested.

    if (state.showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(HomeContract.Event.SetShowNotificationPermissionDialog(false)) },
            title = { Text("Bildirishnomalarga ruxsat bering") },
            text = { Text("Yangi band qilingan vaqtlar va muhim yangiliklardan xabardor bo'lish uchun bildirishnomalarga ruxsat berishingizni so'raymiz.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.handleEvent(HomeContract.Event.RequestNotificationPermission)
                    // Actual request logic would be triggered here
                }) {
                    Text("Ruxsat berish")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(HomeContract.Event.SetShowNotificationPermissionDialog(false)) }) {
                    Text("Keyinroq")
                }
            }
        )
    }

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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(
                    LayoutDirection.Ltr), end = padding.calculateEndPadding(LayoutDirection.Rtl))
        ) {
            if (state.isLoadingUser && state.userRole == UserRole.UNKNOWN) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                when (state.currentTab) {
                    0 -> {
                        when (state.userRole) {
                            UserRole.DISTRICT_ADMIN, UserRole.SUPER_ADMIN -> {
                                HomeTab(
                                    state = state,
                                    onAddStadium = { navigator.push(AddPitchVoyager) },
                                    onAddUser = { navigator.push(uz.coder.foottopbusiness.presentation.main.home.user.UserCreateTypeScreen()) },
                                    onAddTournament = { navigator.push(TournamentsVoyager) },
                                    onProfileClick = { navigator.push(SettingsVoyager) },
                                    onNotificationClick = { navigator.push(SendNotificationVoyager) }
                                )
                            }

                            UserRole.OWNER -> {
                                OwnerHomeTab(
                                    state = state,
                                    onAddStadium = { navigator.push(AddPitchVoyager) },
                                    onAddTournament = { navigator.push(TournamentsVoyager) },
                                    onAddCoach = { /* navigator.push(uz.coder.foottopbusiness.presentation.main.coaches.create.CoachCreateScreen()) */ },
                                    onProfileClick = { navigator.push(SettingsVoyager) },
                                    onNotificationClick = { navigator.push(SendNotificationVoyager) }
                                )
                            }

                            else -> {
                                // For other roles like COACH or USER, show a restricted view
                                RestrictedAccessView(
                                    role = state.userRole,
                                    onRefresh = { viewModel.handleEvent(HomeContract.Event.Load) },
                                    onLogout = { viewModel.handleEvent(HomeContract.Event.Logout) }
                                )
                            }
                        }
                    }

                    1 -> HistoryScreen(state)
                }
            }
        }
    }
}

@Composable
private fun HomeTab(
    state: HomeContract.State,
    onAddStadium: () -> Unit,
    onAddUser: () -> Unit,
    onAddTournament: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            MalaebHeader(state, onProfileClick, onNotificationClick)
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                MalaebDashboard(state)
                
                Spacer(Modifier.height(24.dp))

                Text(
                    "Boshqaruv",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                QuickActionsGrid(
                    onAddStadium = onAddStadium,
                    onAddUser = onAddUser,
                    onAddTournament = onAddTournament
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(top = statusBarPadding, start = 24.dp, end = 24.dp, bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (state.isAdmin) "Tizim Boshqaruvi" else "Mening Stadionim",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    if (state.isAdmin) "Xush kelibsiz!" else state.stadiums.firstOrNull()?.name ?: "FootTop",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeaderIconButton(icon = Icons.Default.Notifications, onClick = onNotificationClick)
                HeaderIconButton(icon = Icons.Default.Person, onClick = onProfileClick)
            }
        }
    }
}

@Composable
private fun HeaderIconButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
        modifier = Modifier.size(46.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun OwnerHomeTab(
    state: HomeContract.State,
    onAddStadium: () -> Unit,
    onAddTournament: () -> Unit,
    onAddCoach: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            MalaebHeader(state, onProfileClick, onNotificationClick)
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                OwnerDashboard(state)
                
                Spacer(Modifier.height(24.dp))

                Text(
                    "Tezkor amallar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                QuickActionsGrid(
                    onAddStadium = onAddStadium,
                    onAddUser = null,
                    onAddTournament = onAddTournament
                )

                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Bugungi jadval",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(Modifier.height(16.dp))
                
                ScheduleList(state)
                
                Spacer(Modifier.height(24.dp))

                /* if (state.isLoadingCoaches) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (state.coaches.isEmpty()) {
                    Text("Hozircha coachlar yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.coaches.take(5).forEach { coach ->
                            UserCardItem(
                                name = coach.coachName ?: "Noma'lum",
                                role = coach.specialty ?: "Sport",
                                status = coach.availability ?: "Aktiv",
                                onClick = {}
                            )
                        }
                    }
                } */
            }
        }
    }
}

@Composable
private fun OwnerDashboard(state: HomeContract.State) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "STADIONLAR",
                value = "${state.activeStadiums}",
                subValue = "Aktiv maydonlar",
                icon = Icons.Default.SportsSoccer,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "FOYDALANUVCHILAR",
                value = "${state.totalUsers}",
                subValue = "Jami a'zolar",
                icon = Icons.Default.Groups,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "TURNIRLAR",
                value = "${state.totalTournaments}",
                subValue = "Jami tadbirlar",
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "DAROMAD",
                value = if (state.totalEarnings > 1000000) "${(state.totalEarnings / 1000000).toInt()}M" else "${state.totalEarnings.toInt()}",
                subValue = "Daromad",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MalaebDashboard(state: HomeContract.State) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "STADIONLAR",
                value = "${state.activeStadiums}",
                subValue = "Aktiv maydonlar",
                icon = Icons.Default.SportsSoccer,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "FOYDALANUVCHILAR",
                value = "${state.totalUsers}",
                subValue = "Jami a'zolar",
                icon = Icons.Default.Groups,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "TURNIRLAR",
                value = "${state.totalTournaments}",
                subValue = "Jami tadbirlar",
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "DAROMAD",
                value = if (state.totalEarnings > 1000000) "${(state.totalEarnings / 1000000).toInt()}M" else "${state.totalEarnings.toInt()}",
                subValue = "Daromad",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScheduleList(state: HomeContract.State) {
    val bookedMatches = state.matches

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.isLoadingMatches) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else if (bookedMatches.isEmpty()) {
            Text(
                "Bugun uchun bandlar yo'q",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        } else {
            bookedMatches.take(5).forEach { match ->
                val time = match.dateTime?.split("T")?.lastOrNull()?.take(5) ?: "00:00"
                ReportItem(
                    title = "$time - ${match.title ?: "Jamoa"}",
                    subtitle = "Maydon #${match.stadiumId ?: 1} • ${match.pricePerPlayer ?: 0.0} UZS",
                    icon = Icons.Default.SportsSoccer,
                    iconBgColor = Color(0xFF26A69A)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Decorative Circle
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
            )

            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Column {
                    Text(
                        value,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        subValue,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onAddStadium: () -> Unit,
    onAddUser: (() -> Unit)?,
    onAddTournament: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem("Stadion qo'sh", Icons.Default.Home, primary, Modifier.weight(1f), onAddStadium)
            if (onAddUser != null) {
                QuickActionItem("Xodim qo'shish", Icons.Default.AddCircle, primary, Modifier.weight(1f), onAddUser)
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem("Turnir yarat", Icons.Default.Add, primary, Modifier.weight(1f), onAddTournament)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionItem(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RestrictedAccessView(role: UserRole, onRefresh: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "Kirish cheklangan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            if (role == UserRole.UNKNOWN) 
                "Sizning profilingiz yuklanmoqda yoki ruxsatnomalar aniqlanmadi." 
            else 
                "Sizning hisobingiz (${role.name}) ushbu panelga kirish huquqiga ega emas.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Yangilash")
        }
        
        Spacer(Modifier.height(16.dp))
        
        TextButton(onClick = onLogout) {
            Text("Boshqa hisobga o'tish", color = MaterialTheme.colorScheme.error)
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
