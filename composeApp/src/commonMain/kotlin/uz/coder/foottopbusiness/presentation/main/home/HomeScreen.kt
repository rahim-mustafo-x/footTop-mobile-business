package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.platform.exitApp
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.shimmer
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.presentation.main.home.history.HistoryScreen
import uz.coder.foottopbusiness.presentation.main.reports.ReportItem
import uz.coder.foottopbusiness.presentation.main.settings.SettingsVoyager
import uz.coder.foottopbusiness.presentation.main.settings.notification.SendNotificationVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.AddStadiumVoyager
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsVoyager
import uz.coder.foottopbusiness.presentation.main.booking.list.BookingListVoyager
import uz.coder.foottopbusiness.core.platform.NotificationPermissionLauncher
import uz.coder.foottopbusiness.core.ui.Info
import uz.coder.foottopbusiness.core.ui.Success
import uz.coder.foottopbusiness.core.ui.Warning

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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.handleEvent(HomeContract.Event.Refresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (state.showNotificationPermissionDialog) {
        NotificationPermissionExplanationDialog(
            onConfirm = { viewModel.handleEvent(HomeContract.Event.RequestNotificationPermission) },
            onDismiss = { viewModel.handleEvent(HomeContract.Event.SetShowNotificationPermissionDialog(false)) }
        )
    }

    if (state.showPermanentlyDeniedDialog) {
        PermanentlyDeniedDialog(
            onOpenSettings = { viewModel.handleEvent(HomeContract.Event.OpenSettings) },
            onDismiss = { viewModel.handleEvent(HomeContract.Event.DismissPermanentlyDeniedDialog) }
        )
    }

    NotificationPermissionLauncher(
        trigger = state.triggerNotificationRequest,
        onResult = { status ->
            viewModel.handleEvent(HomeContract.Event.OnNotificationPermissionResult(status))
        }
    )

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
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoadingUser && state.userRole == UserRole.UNKNOWN) {
                HomeShimmer()
            } else {
                when (state.userRole) {
                    UserRole.DISTRICT_ADMIN, UserRole.SUPER_ADMIN -> {
                        AdminHomeTab(
                            state = state,
                            onAddStadium = { navigator.push(AddStadiumVoyager) },
                            onAddUser = { navigator.push(uz.coder.foottopbusiness.presentation.main.home.user.UserCreateScreen()) },
                            onAddTournament = { navigator.push(TournamentsVoyager) },
                            onProfileClick = { navigator.push(SettingsVoyager) },
                            onNotificationClick = {
                                viewModel.handleEvent(HomeContract.Event.CheckNotificationPermission)
                                navigator.push(SendNotificationVoyager)
                            },
                            onShowBookings = { navigator.push(BookingListVoyager()) }
                        )
                    }

                    UserRole.OWNER -> {
                        OwnerHomeTab(
                            state = state,
                            onAddStadium = { navigator.push(AddStadiumVoyager) },
                            onAddTournament = { navigator.push(TournamentsVoyager) },
                            onAddCoach = { navigator.push(uz.coder.foottopbusiness.presentation.main.home.user.UserCreateScreen()) },
                            onProfileClick = { navigator.push(SettingsVoyager) },
                            onNotificationClick = {
                                viewModel.handleEvent(HomeContract.Event.CheckNotificationPermission)
                            },
                            onShowBookings = { navigator.push(BookingListVoyager()) }
                        )
                    }

                    else -> {
                        UserHomeTab(
                            state = state,
                            onProfileClick = { navigator.push(SettingsVoyager) },
                            onNotificationClick = {
                                viewModel.handleEvent(HomeContract.Event.CheckNotificationPermission)
                            },
                            onRefresh = { viewModel.handleEvent(HomeContract.Event.Load) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHomeTab(
    state: HomeContract.State,
    onAddStadium: () -> Unit,
    onAddUser: () -> Unit,
    onAddTournament: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onShowBookings: () -> Unit
) {
    val strings = Localization.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            MalaebHeader(state, onProfileClick, onNotificationClick)
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                DashboardGrid(state)
                
                Spacer(Modifier.height(24.dp))

                SectionHeader(strings.management, onAction = null)

                Spacer(Modifier.height(16.dp))

                QuickActionsGrid(
                    onAddStadium = onAddStadium,
                    onAddUser = onAddUser,
                    onAddCoach = null,
                    onAddTournament = onAddTournament,
                    onShowBookings = onShowBookings
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onAction: (() -> Unit)? = null) {
    val strings = Localization.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            title,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onAction != null) {
            Text(
                strings.seeAll,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
private fun DashboardGrid(state: HomeContract.State) {
    val strings = Localization.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                value = "${state.activeStadiums}",
                subValue = strings.stadiums,
                icon = Icons.Outlined.SportsSoccer,
                color = Success,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "${state.totalUsers}",
                subValue = strings.users,
                icon = Icons.Outlined.Groups,
                color = Info,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                value = "${state.totalTournaments}",
                subValue = strings.tournaments,
                icon = Icons.Outlined.EmojiEvents,
                color = Warning,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = if (state.totalEarnings > 1000000) "${(state.totalEarnings / 1000000).toInt()}M" else "${state.totalEarnings.toInt()}",
                subValue = strings.revenue,
                icon = Icons.Outlined.AttachMoney,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MalaebHeader(state: HomeContract.State, onProfileClick: () -> Unit, onNotificationClick: () -> Unit) {
    val strings = Localization.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    strings.welcome,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    state.user?.fullName ?: strings.tabHome,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.isAdmin) {
                    HeaderIconButton(icon = Icons.Outlined.Notifications, onClick = onNotificationClick)
                }
                HeaderIconButton(icon = Icons.Outlined.Person, onClick = onProfileClick)
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
private fun StatCard(
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                    }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
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
    onAddCoach: (() -> Unit)?,
    onAddTournament: () -> Unit,
    onShowBookings: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val strings = Localization.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem(strings.addStadium, Icons.Outlined.Home, primary, Modifier.weight(1f), onAddStadium)
            QuickActionItem(strings.bookings, Icons.Outlined.CalendarToday, primary, Modifier.weight(1f), onShowBookings)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (onAddUser != null) {
                QuickActionItem(strings.addEmployee, Icons.Outlined.PersonAdd, primary, Modifier.weight(1f), onAddUser)
            } else if (onAddCoach != null) {
                QuickActionItem(strings.addCoach, Icons.Outlined.Sports, primary, Modifier.weight(1f), onAddCoach)
            } else {
                QuickActionItem(strings.createTournament, Icons.Outlined.EmojiEvents, primary, Modifier.weight(1f), onAddTournament)
            }
            
            if (onAddUser != null || onAddCoach != null) {
                QuickActionItem(strings.createTournament, Icons.Outlined.EmojiEvents, primary, Modifier.weight(1f), onAddTournament)
            } else {
                Spacer(Modifier.weight(1f))
            }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
private fun OwnerHomeTab(
    state: HomeContract.State,
    onAddStadium: () -> Unit,
    onAddTournament: () -> Unit,
    onAddCoach: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onShowBookings: () -> Unit
) {
    val strings = Localization.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            MalaebHeader(state, onProfileClick, onNotificationClick)
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                DashboardGrid(state)
                
                Spacer(Modifier.height(24.dp))

                SectionHeader(strings.quickActions)

                Spacer(Modifier.height(16.dp))

                QuickActionsGrid(
                    onAddStadium = onAddStadium,
                    onAddUser = null,
                    onAddCoach = onAddCoach,
                    onAddTournament = onAddTournament,
                    onShowBookings = onShowBookings
                )

                Spacer(Modifier.height(32.dp))
                
                SectionHeader(strings.todaySchedule)
                
                Spacer(Modifier.height(16.dp))
                
                ScheduleList(state)
            }
        }
    }
}

@Composable
private fun UserHomeTab(
    state: HomeContract.State,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val strings = Localization.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            MalaebHeader(state, onProfileClick, onNotificationClick)
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(strings.tournaments)
                
                Spacer(Modifier.height(16.dp))
                
                if (state.isLoadingTournaments) {
                    repeat(2) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(24.dp)).shimmer().padding(bottom = 12.dp))
                    }
                } else if (state.tournaments.isEmpty()) {
                    Text(strings.noTournaments, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                } else {
                    state.tournaments.take(3).forEach { tournament ->
                        TournamentCard(tournament)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = strings.refresh)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.refresh)
                }
            }
        }
    }
}

@Composable
private fun TournamentCard(tournament: TournamentResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(tournament.name ?: "", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(tournament.address ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ScheduleList(state: HomeContract.State) {
    val bookedMatches = state.matches
    val strings = Localization.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.isLoadingMatches) {
            repeat(3) {
                Box(modifier = Modifier.fillMaxWidth().height(76.dp).clip(RoundedCornerShape(16.dp)).shimmer())
            }
        } else if (bookedMatches.isEmpty()) {
            Text(
                strings.noBookingsToday,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        } else {
            bookedMatches.take(5).forEach { match ->
                val localDateTime = match.dateTime.toLocalDateTimeSafe()
                val time = localDateTime?.let { 
                    "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
                } ?: "00:00"

                ReportItem(
                    title = "$time - ${match.title ?: strings.team}",
                    subtitle = "${strings.field} #${match.stadiumId ?: 1} • ${match.pricePerPlayer ?: 0.0} so'm",
                    icon = Icons.Outlined.SportsSoccer,
                    iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    onClick = { /* Navigation or Action */ }
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionExplanationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val strings = Localization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notificationRationaleTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.notificationRationaleDesc)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BenefitItem(strings.notificationBenefit1)
                    BenefitItem(strings.notificationBenefit2)
                    BenefitItem(strings.notificationBenefit3)
                    BenefitItem(strings.notificationBenefit4)
                }
                Text(
                    text = "${strings.enableNotifications}?",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.enableNotifications)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.maybeLater)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.Check, 
            null, 
            tint = Success, 
            modifier = Modifier.size(20.dp)
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PermanentlyDeniedDialog(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val strings = Localization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notificationsDeniedTitle) },
        text = { Text(strings.notificationsDeniedDesc) },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.openSettings)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentDetailScreen(tournament: TournamentResponseDto, onBack: () -> Unit) {
    val strings = Localization.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.tournamentDetails) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(tournament.name ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(tournament.sportType ?: "FOOTBALL", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            item {
                InfoSection(strings.location, tournament.address ?: strings.noDataYet, Icons.Default.LocationOn)
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoSection(strings.tournamentDate, "${tournament.startDate} - ${tournament.endDate}", Icons.Default.CalendarToday, Modifier.weight(1f))
                    val startTime = tournament.startTime?.toString() ?: ""
                    val endTime = tournament.endTime?.toString() ?: ""
                    InfoSection(strings.tournamentTime, "$startTime - $endTime", Icons.Default.AccessTime, Modifier.weight(1f))
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoSection(strings.participants, "${tournament.teamApplied ?: 0} / ${tournament.maxTeams ?: 0}", Icons.Default.Groups, Modifier.weight(1f))
                    InfoSection(strings.entryFee, "${tournament.entryFee?.toInt() ?: 0} so'm", Icons.Default.Payments, Modifier.weight(1f))
                }
            }

            item {
                InfoSection(strings.prizes, tournament.prizes ?: strings.noDataYet, Icons.Default.EmojiEvents)
            }

            item {
                InfoSection(strings.rules, tournament.rules ?: strings.noDataYet, Icons.Default.Description)
            }
            
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun InfoSection(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(value, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HomeShimmer() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .shimmer()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(24.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(24.dp)).shimmer())
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(24.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(24.dp)).shimmer())
            }
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.width(150.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(24.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(24.dp)).shimmer())
            }
        }
    }
}
