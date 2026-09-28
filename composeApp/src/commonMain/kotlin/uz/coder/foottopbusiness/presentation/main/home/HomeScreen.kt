package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import uz.coder.foottopbusiness.core.Money
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.core.minutesBetween
import uz.coder.foottopbusiness.core.plusMinutes
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.core.ui.AppCard
import uz.coder.foottopbusiness.core.ui.AppCardShape
import uz.coder.foottopbusiness.core.ui.IconBadge
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.ui.HeaderIconButton
import uz.coder.foottopbusiness.core.ui.RoleBadge
import uz.coder.foottopbusiness.core.ui.scopeText
import uz.coder.foottopbusiness.core.ui.StatCardHeight
import uz.coder.foottopbusiness.core.ui.shimmer
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.presentation.main.reports.ReportItem
import uz.coder.foottopbusiness.presentation.main.settings.SettingsVoyager
import uz.coder.foottopbusiness.presentation.main.settings.notification.SendNotificationVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.AddStadiumVoyager
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentDetailContent
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
    var showStadiumPicker by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        when {
            state.selectedTournament != null -> viewModel.handleEvent(HomeContract.Event.ClearTournament)
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

    // Yagona collector: effect Channel asosida, shuning uchun uni faqat
    // shu yerda o'qiymiz (HomeVoyager'da takroriy collect bo'lmasin).
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

    if (showStadiumPicker) {
        StadiumPickerSheet(
            stadiums = state.stadiums,
            onDismiss = { showStadiumPicker = false },
            onSelect = { stadium ->
                showStadiumPicker = false
                navigateToSlotsControl(stadium)
            }
        )
    }

    NotificationPermissionLauncher(
        trigger = state.triggerNotificationRequest,
        onResult = { status ->
            viewModel.handleEvent(HomeContract.Event.OnNotificationPermissionResult(status))
        }
    )

    // Diqqat: bu yerda `selectedStadiumForTime` bo'yicha avtomatik o'tish
    // qilinmaydi. O'sha state'ni SlotsControlVoyager'ning o'zi o'rnatadi -
    // agar tizim "orqaga" tugmasi bilan chiqilsa u tozalanmay qoladi va
    // HomeScreen qayta ko'rinishi bilanoq ekranni cheksiz qayta ochib
    // yuborardi. O'tish faqat aniq amal orqali - stadion tanlanganda.

    state.selectedTournament?.let { t ->
        TournamentDetailContent(t, onBack = { viewModel.handleEvent(HomeContract.Event.ClearTournament) })
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Gradient header status bar ostiga cho'ziladi va tepa bo'shliqni
        // GradientHeader o'zi hisoblaydi - shuning uchun bu yerda faqat pastki inset
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Rol aniqlanmagan bo'lsa haqiqiy sarlavhani chizmaymiz: aks holda
            // bir zumga "rol aniqlanmagan" nishonchali, ko'lam qatorisiz
            // header chiqib, keyin rol kelganda balandligi o'zgarib ketadi.
            if (state.userRole == UserRole.UNKNOWN) {
                HomeShimmer()
            } else {
                when (state.userRole) {
                    UserRole.DISTRICT_ADMIN, UserRole.SUPER_ADMIN -> {
                        AdminHomeTab(
                            state = state,
                            onAddStadium = { navigator.push(AddStadiumVoyager()) },
                            onAddUser = { navigator.push(uz.coder.foottopbusiness.presentation.main.home.user.UserCreateScreen()) },
                            onAddTournament = { navigator.push(TournamentsVoyager) },
                            onProfileClick = { navigator.push(SettingsVoyager) },
                            onSendMessage = {
                                viewModel.handleEvent(HomeContract.Event.CheckNotificationPermission)
                                navigator.push(SendNotificationVoyager)
                            },
                            onShowBookings = { navigator.push(BookingListVoyager()) }
                        )
                    }

                    UserRole.OWNER -> {
                        OwnerHomeTab(
                            state = state,
                            onAddStadium = { navigator.push(AddStadiumVoyager()) },
                            onAddTournament = { navigator.push(TournamentsVoyager) },
                            onProfileClick = { navigator.push(SettingsVoyager) },
                            onShowBookings = { navigator.push(BookingListVoyager()) },
                            onCreateBooking = {
                                // Bitta stadion bo'lsa tanlash oynasi ortiqcha
                                val onlyStadium = state.stadiums.singleOrNull()
                                if (onlyStadium != null) {
                                    navigateToSlotsControl(onlyStadium)
                                } else {
                                    showStadiumPicker = true
                                }
                            }
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

/**
 * Super admin va tuman admini bosh sahifasi.
 *
 * Ikkalasining vazifalari bir xil (xodim, stadion, turnir qo'shish, xabar
 * yuborish), farqi faqat ko'lamda - u sarlavhada rol nishonchasi yonida
 * yoziladi. Adminning asosiy ishi xodim va stadion qo'shish, shuning uchun
 * ular eng katta tugmalar.
 */
@Composable
private fun AdminHomeTab(
    state: HomeContract.State,
    onAddStadium: () -> Unit,
    onAddUser: () -> Unit,
    onAddTournament: () -> Unit,
    onProfileClick: () -> Unit,
    onSendMessage: () -> Unit,
    onShowBookings: () -> Unit
) {
    val strings = Localization.current
    val now = rememberCurrentMinute()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            GreetingHeader(state, now.date, onProfileClick)
        }

        item {
            AdminStatsGrid(state)
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BigActionTile(
                    title = strings.addEmployee,
                    // UserCreateScreen'dagi availableRoles bilan mos: kimni yarata oladi
                    subtitle = if (state.userRole == UserRole.SUPER_ADMIN) {
                        strings.addEmployeeHintSuperAdmin
                    } else {
                        strings.addEmployeeHintDistrictAdmin
                    },
                    icon = Icons.Outlined.PersonAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    iconContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onAddUser,
                    modifier = Modifier.weight(1f)
                )
                BigActionTile(
                    title = strings.addStadium,
                    subtitle = strings.addStadiumHint,
                    icon = Icons.Outlined.Home,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconContainerColor = MaterialTheme.colorScheme.tertiary,
                    iconColor = MaterialTheme.colorScheme.onTertiary,
                    onClick = onAddStadium,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallActionTile(strings.bookings, Icons.Outlined.CalendarToday, Info, onShowBookings, Modifier.weight(1f))
                SmallActionTile(strings.createTournament, Icons.Outlined.EmojiEvents, Warning, onAddTournament, Modifier.weight(1f))
                SmallActionTile(strings.sendMessage, Icons.Outlined.Campaign, Success, onSendMessage, Modifier.weight(1f))
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(strings.revenueByStadium)
                StadiumRevenueList(state)
            }
        }
    }
}

@Composable
private fun AdminStatsGrid(state: HomeContract.State) {
    val strings = Localization.current
    Column(
        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatTile(
                value = "${state.activeStadiums}",
                label = strings.activeStadiums,
                icon = Icons.Outlined.SportsSoccer,
                color = Success,
                modifier = Modifier.weight(1f)
            )
            AdminStatTile(
                value = "${state.totalUsers}",
                label = strings.usersLabel,
                icon = Icons.Outlined.Groups,
                color = Info,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatTile(
                value = "${state.totalTournaments}",
                label = strings.totalTournaments,
                icon = Icons.Outlined.EmojiEvents,
                color = Warning,
                modifier = Modifier.weight(1f)
            )
            AdminStatTile(
                value = Money.compactWithCurrency(state.totalEarnings, strings.currency),
                label = strings.totalRevenue,
                icon = Icons.Outlined.Payments,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Statistika kartasi: raqam va uning nimani anglatishi to'liq so'z bilan.
 * Umumiy StatCard'dagi mayda (10sp) izoh bosh sahifada o'qilmay qolardi.
 */
@Composable
private fun AdminStatTile(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(icon = icon, color = color, size = 40.dp, iconSize = 20.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    label,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Daromadi eng yuqori 5 ta stadion, eng kattasiga nisbatan chiziq bilan. */
@Composable
private fun StadiumRevenueList(state: HomeContract.State) {
    val strings = Localization.current
    val top = remember(state.stadiumRevenues) {
        state.stadiumRevenues.filter { it.totalRevenue > 0 }.sortedByDescending { it.totalRevenue }.take(5)
    }
    // Nomlar stadionlar ro'yxatining birinchi sahifasidan olinadi - topilmasa raqami ko'rsatiladi
    val names = remember(state.stadiums) { state.stadiums.associate { it.id to it.name } }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        if (top.isEmpty()) {
            Text(
                strings.noRevenueYet,
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            return@AppCard
        }
        val max = top.first().totalRevenue
        top.forEachIndexed { index, revenue ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${index + 1}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(16.dp)
                    )
                    Text(
                        names[revenue.stadiumId] ?: "${strings.stadium} #${revenue.stadiumId}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        Money.compactWithCurrency(revenue.totalRevenue, strings.currency),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                LinearProgressIndicator(
                    progress = { (revenue.totalRevenue / max).toFloat() },
                    modifier = Modifier.fillMaxWidth().padding(start = 26.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    drawStopIndicator = {}
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
private fun MalaebHeader(state: HomeContract.State, onProfileClick: () -> Unit, onNotificationClick: () -> Unit) {
    val strings = Localization.current
    GradientHeader(
        // Ilgari bu yerda faqat "Xush kelibsiz" turardi va foydalanuvchi
        // o'zining qaysi rolda ekanini UI'dan bilolmasdi.
        badge = { RoleBadge(state.userRole, onGradient = true) },
        title = state.user?.fullName ?: strings.tabHome,
        subtitle = state.userRole.scopeText(strings, state.user, state.activeStadiums),
        titleFontSize = 24.sp,
        actions = {
            if (state.isAdmin) {
                HeaderIconButton(
                    icon = Icons.Outlined.Notifications,
                    onClick = onNotificationClick,
                    contentDescription = strings.notifications,
                    shape = CircleShape
                )
            }
            HeaderIconButton(
                icon = Icons.Outlined.Person,
                onClick = onProfileClick,
                contentDescription = strings.profile,
                shape = CircleShape
            )
        }
    )
}

/**
 * "Bron qilish" bosilganda stadionni tanlash paneli.
 *
 * Bron qilish oqimi ([SlotsControlVoyager]) stadionsiz ochilmaydi, egada esa
 * bir nechta stadion bo'lishi mumkin - shuning uchun oraliq tanlov kerak.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StadiumPickerSheet(
    stadiums: List<StadiumResponse>,
    onDismiss: () -> Unit,
    onSelect: (StadiumResponse) -> Unit
) {
    val strings = Localization.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                strings.selectStadiumForBooking,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (stadiums.isEmpty()) {
                Text(
                    strings.noStadiumsToBook,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                stadiums.forEach { stadium ->
                    ReportItem(
                        title = stadium.name ?: strings.stadium,
                        subtitle = "${stadium.districtName}, ${stadium.regionName}",
                        icon = Icons.Outlined.SportsSoccer,
                        iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        onClick = { onSelect(stadium) }
                    )
                }
            }
        }
    }
}

/**
 * Stadion egasining bosh sahifasi.
 *
 * Ega kuniga eng ko'p ikki narsa qiladi: bron qiladi va bugungi bronlarni
 * ko'radi - shuning uchun ular eng katta tugmalar. Qolgan amallar kichikroq,
 * ostida esa keyingi o'yin va bugungi qolgan o'yinlar turadi.
 */
@Composable
private fun OwnerHomeTab(
    state: HomeContract.State,
    onAddStadium: () -> Unit,
    onAddTournament: () -> Unit,
    onProfileClick: () -> Unit,
    onShowBookings: () -> Unit,
    onCreateBooking: () -> Unit
) {
    val strings = Localization.current
    val now = rememberCurrentMinute()
    val today = remember(state.matches, state.stadiums, now.date) {
        todaySchedule(state.matches, state.stadiums, now.date)
    }
    // Hali tugamagan birinchi o'yin - davom etayotgan yoki navbatdagisi
    val nextIndex = today.indexOfFirst { it.end > now }
    val next = today.getOrNull(nextIndex)
    val later = if (nextIndex >= 0) today.drop(nextIndex + 1) else emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            GreetingHeader(state, now.date, onProfileClick)
        }

        item {
            OwnerStatChips(
                todayCount = today.size,
                earnings = state.totalEarnings,
                stadiumCount = state.activeStadiums
            )
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BigActionTile(
                    title = strings.createBooking,
                    subtitle = strings.createBookingHint,
                    icon = Icons.Default.Add,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    iconContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onCreateBooking,
                    modifier = Modifier.weight(1f)
                )
                BigActionTile(
                    title = strings.bookings,
                    subtitle = strings.todayBookingsHint(today.size),
                    icon = Icons.Outlined.CalendarToday,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconContainerColor = MaterialTheme.colorScheme.tertiary,
                    iconColor = MaterialTheme.colorScheme.onTertiary,
                    onClick = onShowBookings,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallActionTile(strings.addStadium, Icons.Outlined.Home, Info, onAddStadium, Modifier.weight(1f))
                SmallActionTile(strings.createTournament, Icons.Outlined.EmojiEvents, Warning, onAddTournament, Modifier.weight(1f))
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(strings.nextMatch)
                when {
                    state.isLoadingMatches && state.matches.isEmpty() -> Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(AppCardShape).shimmer()
                    )
                    next != null -> NextMatchCard(next, now)
                    else -> AppCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (today.isEmpty()) strings.noBookingsToday else strings.noMoreMatchesToday,
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        if (later.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        strings.laterToday,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onShowBookings) {
                        Text(strings.seeAllCount(today.size), fontWeight = FontWeight.Bold)
                    }
                }
            }
            items(later.take(3)) { match ->
                LaterMatchRow(match, modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp))
            }
        }
    }
}

/** Bugungi bitta o'yin - jadvalda ko'rsatish uchun tayyor ko'rinishda. */
private data class ScheduledMatch(
    val title: String?,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val minutes: Int,
    val stadiumName: String?,
    val stadiumId: Long?,
    val price: Double?
)

private fun todaySchedule(
    matches: List<MatchResponseDto>,
    stadiums: List<StadiumResponse>,
    date: LocalDate
): List<ScheduledMatch> {
    val stadiumNames = stadiums.associate { it.id?.toLong() to it.name }
    return matches.mapNotNull { match ->
        val start = match.dateTime.toLocalDateTimeSafe() ?: return@mapNotNull null
        if (start.date != date) return@mapNotNull null
        val minutes = durationMinutesKey(match.duration ?: "")
        ScheduledMatch(
            title = match.title,
            start = start,
            end = start.plusMinutes(minutes),
            minutes = minutes,
            stadiumName = stadiumNames[match.stadiumId],
            stadiumId = match.stadiumId,
            price = match.pricePerPlayer
        )
    }.sortedBy { it.start }
}

private fun currentDateTime(): LocalDateTime =
    kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/** Joriy vaqt, har daqiqa boshida yangilanadi - "... dan so'ng" matni eskirmasin. */
@Composable
private fun rememberCurrentMinute(): LocalDateTime {
    var now by remember { mutableStateOf(currentDateTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - kotlin.time.Clock.System.now().toEpochMilliseconds() % 60_000L)
            now = currentDateTime()
        }
    }
    return now
}

private fun LocalDateTime.hhmm(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

@Composable
private fun GreetingHeader(
    state: HomeContract.State,
    date: LocalDate,
    onProfileClick: () -> Unit
) {
    val strings = Localization.current
    val fullName = state.user?.fullName?.trim().orEmpty()
    val nameParts = fullName.split(" ").filter { it.isNotBlank() }
    val initials = nameParts.take(2).joinToString("") { it.first().uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                strings.longDate(date),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                nameParts.firstOrNull()?.let { strings.greeting(it) } ?: strings.tabHome,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Foydalanuvchi qaysi rolda ekanini bosh sahifadan bilib tursin.
            // Adminlarda yonida ko'lam ham yoziladi ("Butun tizim" yoki tuman nomi) -
            // super admin bilan tuman admini aynan shu bilan farqlanadi.
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleBadge(state.userRole)
                if (state.isAdmin) {
                    state.userRole.scopeText(strings, state.user)?.let { scope ->
                        Text(
                            scope,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Surface(
            onClick = onProfileClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp).semantics { contentDescription = strings.profile }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (initials.isNotEmpty()) {
                    Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun OwnerStatChips(todayCount: Int, earnings: Double, stadiumCount: Int) {
    val strings = Localization.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(label = strings.today, value = strings.bookingCount(todayCount))
        StatChip(
            label = strings.totalRevenue,
            value = Money.compactWithCurrency(earnings, strings.currency),
            valueColor = MaterialTheme.colorScheme.tertiary
        )
        StatChip(label = null, value = strings.stadiumCount(stadiumCount))
    }
}

@Composable
private fun StatChip(
    label: String?,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (label != null) {
                Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun BigActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    iconContainerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier.height(170.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = iconContainerColor, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 13.sp, maxLines = 2, color = contentColor.copy(alpha = 0.85f))
            }
        }
    }
}

@Composable
private fun SmallActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.height(112.dp), onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconBadge(icon = icon, color = color, size = 44.dp)
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NextMatchCard(match: ScheduledMatch, now: LocalDateTime) {
    val strings = Localization.current
    val minutesLeft = minutesBetween(now, match.start)

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).widthIn(min = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            match.start.hhmm(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            strings.untilTime(match.end.hhmm()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        match.title ?: strings.team,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Place, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            match.stadiumName ?: "${strings.field} #${match.stadiumId ?: 1}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    match.price?.let {
                        Text(
                            Money.withCurrency(it, strings.currency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp))
                    Text(
                        if (minutesLeft <= 0) strings.matchInProgress
                        else strings.startsIn(minutesLeft / 60, minutesLeft % 60),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LaterMatchRow(match: ScheduledMatch, modifier: Modifier = Modifier) {
    val strings = Localization.current
    AppCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(match.start.hhmm(), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(48.dp))
            Text(
                match.title ?: strings.team,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                strings.minutesShort(match.minutes),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    AppCard(modifier = Modifier.fillMaxWidth()) {
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

@Composable
fun HomeShimmer() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Balandlik haqiqiy GradientHeader bilan taxminan bir xil bo'lsin -
        // status bar + nishoncha + sarlavha + ko'lam qatori
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 113.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .shimmer()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(StatCardHeight).clip(RoundedCornerShape(24.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(StatCardHeight).clip(RoundedCornerShape(24.dp)).shimmer())
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(StatCardHeight).clip(RoundedCornerShape(24.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(StatCardHeight).clip(RoundedCornerShape(24.dp)).shimmer())
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
