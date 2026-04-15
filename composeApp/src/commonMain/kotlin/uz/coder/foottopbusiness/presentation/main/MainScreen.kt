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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesVoyager
import uz.coder.foottopbusiness.presentation.main.home.HomeVoyager
import uz.coder.foottopbusiness.presentation.main.reports.ReportsScreen
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumVoyager

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

val LocalBottomBarVisible = staticCompositionLocalOf<MutableState<Boolean>> {
    error("No BottomBarVisible provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val bottomBarVisible = remember { mutableStateOf(true) }
    val homeViewModel = koinInject<HomeViewModel>()
    val homeState by homeViewModel.state.collectAsState()

    CompositionLocalProvider(LocalBottomBarVisible provides bottomBarVisible) {
        TabNavigator(HomeTab) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (bottomBarVisible.value) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            TabNavigationItem(HomeTab, if (homeState.isOwner) "Bosh" else "Panel")
                            TabNavigationItem(StadiumTab, if (homeState.isOwner) "Jadval" else "Stadion")
                            TabNavigationItem(CoachesTab, if (homeState.isOwner) "Coachlar" else "Rollar")
                            TabNavigationItem(ReportsTab, "Daromad")
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    CurrentTabContent(HomeTab)
                    CurrentTabContent(StadiumTab)
                    CurrentTabContent(CoachesTab)
                    CurrentTabContent(ReportsTab)
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
