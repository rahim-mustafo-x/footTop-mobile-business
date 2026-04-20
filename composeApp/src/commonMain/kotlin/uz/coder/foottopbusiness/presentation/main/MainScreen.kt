package uz.coder.foottopbusiness.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import uz.coder.foottopbusiness.domain.model.UserRole
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
    val homeViewModel = koinInject<HomeViewModel>()
    val sessionManager = koinInject<SessionManager>()
    val homeState by homeViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        sessionManager.networkError.collect { error ->
            val baseMessage = when (error.code) {
                401 -> "Sessiya muddati tugadi. Iltimos, qayta kiring (401)"
                403 -> "Sizda bu amalni bajarish uchun ruxsat yo'q (403)"
                404 -> "Ma'lumot topilmadi (404)"
                409 -> "Bunday ma'lumot allaqachon mavjud yoki ziddiyat yuzaga keldi (409)"
                400 -> "Xato so'rov yuborildi (400)"
                500 -> "Serverda xatolik yuz berdi (500)"
                else -> "Tarmoq xatosi: ${error.code}"
            }
            
            val displayMessage = if (!error.details.isNullOrEmpty()) {
                "${error.message ?: baseMessage}\n${error.details.joinToString("\n")}"
            } else if (!error.message.isNullOrBlank()) {
                error.message
            } else {
                baseMessage
            }
            
            snackbarHostState.showSnackbar(displayMessage)
        }
    }

    // Persist role to avoid flickering during state updates
    var persistedRole by rememberSaveable { mutableStateOf(UserRole.UNKNOWN.name) }
    
    LaunchedEffect(homeState.userRole) {
        if (homeState.userRole != UserRole.UNKNOWN) {
            persistedRole = homeState.userRole.name
        }
    }

    val currentRole = UserRole.valueOf(persistedRole)
    val isAdminOrOwner = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.DISTRICT_ADMIN || currentRole == UserRole.OWNER

    CompositionLocalProvider(LocalBottomBarVisible provides bottomBarVisible) {
        TabNavigator(HomeTab) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (bottomBarVisible.value) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            TabNavigationItem(HomeTab, if (currentRole == UserRole.OWNER) "Bosh" else "Panel")
                            TabNavigationItem(StadiumTab, if (currentRole == UserRole.OWNER) "Jadval" else "Stadion")
                            if (isAdminOrOwner) {
                                TabNavigationItem(CoachesTab, if (currentRole == UserRole.OWNER) "Coachlar" else "Rollar")
                            }
                            if (isAdminOrOwner) {
                                TabNavigationItem(ReportsTab, "Daromad")
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    CurrentTabContent(HomeTab)
                    CurrentTabContent(StadiumTab)
                    if (isAdminOrOwner) {
                        CurrentTabContent(CoachesTab)
                    }
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
    
    // Use an Alpha-based approach or similar to keep the state alive but invisible
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
                contentDescription = label ?: tab.options.title
            )
        },
        label = { Text(label ?: tab.options.title, fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
        )
    )
}

internal object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.GridView)
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
            val icon = rememberVectorPainter(Icons.Filled.Place)
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

internal object CoachesTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.Group)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Rollar",
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

internal object ReportsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.Assessment)
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
