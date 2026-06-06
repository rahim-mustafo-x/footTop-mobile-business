package uz.coder.foottopbusiness.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesVoyager
import uz.coder.foottopbusiness.presentation.main.home.HomeVoyager
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel
import uz.coder.foottopbusiness.presentation.main.reports.ReportsScreen
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumVoyager

val LocalBottomBarVisible = staticCompositionLocalOf<MutableState<Boolean>> {
    error("No BottomBarVisible provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val bottomBarVisible = remember { mutableStateOf(true) }
    val userSession = koinInject<UserSession>()
    val strings = Localization.current

    val currentRole by userSession.role.collectAsState()
    val isAdminOrOwner = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.DISTRICT_ADMIN || currentRole == UserRole.OWNER || currentRole == UserRole.UNKNOWN
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalBottomBarVisible provides bottomBarVisible) {
        TabNavigator(HomeTab) {
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
                            TabNavigationItem(HomeTab, if (currentRole == UserRole.OWNER) strings.tabHome else strings.tabPanel)
                            TabNavigationItem(StadiumTab, if (currentRole == UserRole.OWNER) strings.tabSchedule else strings.tabStadium)
                            TabNavigationItem(HistoryTab, "Tarix")
                            
                            if (isAdminOrOwner) {
                                TabNavigationItem(ReportsTab, strings.tabRevenue)
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    CurrentTabContent(HomeTab)
                    CurrentTabContent(StadiumTab)
                    CurrentTabContent(HistoryTab)
                    if (isAdminOrOwner) {
                        CurrentTabContent(ReportsTab)
                    }
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
private fun RowScope.TabNavigationItem(tab: Tab, label: String? = null) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon!!,
                contentDescription = label ?: tab.options.title,
                modifier = Modifier.size(24.dp)
            )
        },
        label = { Text(label ?: tab.options.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

internal object HistoryTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Outlined.History)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Tarix",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val homeViewModel = koinInject<HomeViewModel>()
        val state by homeViewModel.state.collectAsState()
        uz.coder.foottopbusiness.presentation.main.home.history.HistoryScreen(state)
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
