package uz.coder.foottopbusiness.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.AccessGateScreen
import uz.coder.foottopbusiness.presentation.main.booking.list.BookingListVoyager
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesVoyager
import uz.coder.foottopbusiness.presentation.main.home.HomeVoyager
import uz.coder.foottopbusiness.presentation.main.reports.ReportsScreen
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumVoyager

val LocalBottomBarVisible = staticCompositionLocalOf<MutableState<Boolean>> {
    error("No BottomBarVisible provided")
}

/**
 * Bitta rolga tegishli bitta tab: ekran + shu roldagi nomi va ikonkasi.
 *
 * Ikonka tab obyektining o'zidan emas, shu yerdan olinadi - chunki bir xil
 * ekran turli rolda boshqa ma'no bildiradi (admin uchun "Stadionlar" ro'yxati,
 * ega uchun "Jadval").
 */
private data class RoleTab(val tab: Tab, val label: String, val icon: ImageVector)

/**
 * Rolga mos tab to'plami.
 *
 * Ilgari hamma rol bir xil ikonkali ikki-uch tabni ko'rardi va faqat yozuv
 * almashardi - shuning uchun ekranga qarab kim ekanini ajratib bo'lmasdi.
 */
@Composable
private fun tabsForRole(role: UserRole): List<RoleTab> {
    val strings = Localization.current
    return when (role) {
        UserRole.SUPER_ADMIN -> listOf(
            RoleTab(HomeTab, strings.tabPanel, Icons.Outlined.GridView),
            RoleTab(StadiumTab, strings.tabStadium, Icons.Outlined.Place),
            RoleTab(UsersTab, strings.tabUsers, Icons.Outlined.Groups),
            RoleTab(ReportsTab, strings.tabRevenue, Icons.Outlined.Assessment),
        )
        // Tuman admini xodim yaratadi, lekin uning ko'lami bitta tuman -
        // shuning uchun tizim bo'yicha xodimlar ro'yxati tab'i berilmaydi.
        UserRole.DISTRICT_ADMIN -> listOf(
            RoleTab(HomeTab, strings.tabPanel, Icons.Outlined.GridView),
            RoleTab(StadiumTab, strings.tabStadium, Icons.Outlined.Place),
            RoleTab(ReportsTab, strings.tabRevenue, Icons.Outlined.Assessment),
        )
        UserRole.OWNER -> listOf(
            RoleTab(HomeTab, strings.tabHome, Icons.Outlined.Home),
            RoleTab(StadiumTab, strings.tabSchedule, Icons.Outlined.CalendarMonth),
            RoleTab(ReportsTab, strings.tabRevenue, Icons.Outlined.Payments),
        )
        // Murabbiyga stadion ro'yxati emas, bronlar kerak
        UserRole.COACH -> listOf(
            RoleTab(HomeTab, strings.tabHome, Icons.Outlined.Home),
            RoleTab(BookingsTab, strings.tabBookings, Icons.AutoMirrored.Outlined.EventNote),
        )
        // Bu ikkalasi tab olmaydi - AccessGateScreen'ga tushadi
        UserRole.PLAYER, UserRole.UNKNOWN -> emptyList()
    }
}

/** Rol kelmasa shuncha kutamiz, keyin boshi berk ko'cha deb hisoblaymiz. */
private const val RoleWaitTimeoutMillis = 8000L

@Composable
fun MainScreen() {
    val bottomBarVisible = remember { mutableStateOf(true) }
    val userSession = koinInject<UserSession>()
    val strings = Localization.current

    val currentRole by userSession.role.collectAsState()

    // Rol kelguncha kutamiz, lekin cheksiz emas. Splash foydalanuvchini
    // yuklashdagi xatoni yutib yuborishi mumkin - u holda rol hech qachon
    // kelmaydi va ilova ilgari shu yerda muzlab qolardi.
    if (currentRole == UserRole.UNKNOWN) {
        var waitedTooLong by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(RoleWaitTimeoutMillis)
            waitedTooLong = true
        }

        if (waitedTooLong) {
            val accessViewModel = koinInject<AccessViewModel>()
            val isRetrying by accessViewModel.isRetrying.collectAsState()
            AccessGateScreen(
                icon = Icons.Outlined.CloudOff,
                accentColor = MaterialTheme.colorScheme.error,
                title = strings.roleNotLoadedTitle,
                description = strings.roleNotLoadedDesc,
                isRetrying = isRetrying,
                onRetry = { accessViewModel.retry() },
                onLogout = { accessViewModel.logout() }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    // O'yinchining bu ilovada vakolati yo'q. Ilgari u `else` shoxi orqali
    // to'liq admin tab'larini olardi.
    if (currentRole == UserRole.PLAYER) {
        val accessViewModel = koinInject<AccessViewModel>()
        AccessGateScreen(
            icon = Icons.Outlined.Block,
            accentColor = MaterialTheme.colorScheme.error,
            title = strings.accessDeniedTitle,
            description = strings.accessDeniedDesc,
            onLogout = { accessViewModel.logout() }
        )
        return
    }

    val tabs = tabsForRole(currentRole)
    // Kelajakda tab'siz rol qo'shilib qolsa ilova qulab tushmasin
    val firstTab = tabs.firstOrNull() ?: return
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalBottomBarVisible provides bottomBarVisible) {
        TabNavigator(firstTab.tab) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    AnimatedVisibility(
                        visible = bottomBarVisible.value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            tabs.forEach { TabNavigationItem(it) }
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    tabs.forEach { CurrentTabContent(it.tab) }
                }
            }
        }
    }
}

@Composable
fun CurrentTabContent(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab
    
    Box(modifier = Modifier.fillMaxSize(), propagateMinConstraints = true) {
        if (isSelected) {
            tab.Content()
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(item: RoleTab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == item.tab

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = item.tab },
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp)
            )
        },
        label = { Text(item.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
            unselectedTextColor = Color.Gray.copy(alpha = 0.6f),
        )
    )
}

internal object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Outlined.GridView)
            return remember {
                TabOptions(
                    index = 0u,
                    title = "Panel",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val visibility = LocalBottomBarVisible.current
        Navigator(HomeVoyager) { navigator ->
            LaunchedEffect(navigator.size) {
                visibility.value = navigator.size <= 1
            }
            CurrentScreen()
        }
    }
}

internal object StadiumTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Outlined.Place)
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Stadion",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val visibility = LocalBottomBarVisible.current
        Navigator(StadiumVoyager) { navigator ->
            LaunchedEffect(navigator.size) {
                visibility.value = navigator.size <= 1
            }
            CurrentScreen()
        }
    }
}

/** Xodimlar ro'yxati - faqat super admin uchun tab. */
internal object UsersTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Outlined.Groups)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Xodimlar",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val visibility = LocalBottomBarVisible.current
        Navigator(CoachesVoyager) { navigator ->
            LaunchedEffect(navigator.size) {
                visibility.value = navigator.size <= 1
            }
            CurrentScreen()
        }
    }
}

/** Bronlar - murabbiy uchun asosiy ish ekrani. */
internal object BookingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.AutoMirrored.Outlined.EventNote)
            return remember {
                TabOptions(
                    index = 4u,
                    title = "Bronlar",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val visibility = LocalBottomBarVisible.current
        Navigator(BookingListVoyager(isRoot = true)) { navigator ->
            LaunchedEffect(navigator.size) {
                visibility.value = navigator.size <= 1
            }
            CurrentScreen()
        }
    }
}

internal object ReportsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Outlined.Assessment)
            return remember {
                TabOptions(
                    index = 3u,
                    title = "Hisobot",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        ReportsScreen()
    }
}
