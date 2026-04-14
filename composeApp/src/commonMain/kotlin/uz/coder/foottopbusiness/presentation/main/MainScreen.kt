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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.presentation.main.reports.ReportsScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesVoyager
import uz.coder.foottopbusiness.presentation.main.home.HomeVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumVoyager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    TabNavigator(HomeTab) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    TabNavigationItem(HomeTab)
                    TabNavigationItem(StadiumTab)
                    TabNavigationItem(CoachesTab)
                    TabNavigationItem(ReportsTab)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                CurrentTab()
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon!!,
                contentDescription = tab.options.title
            )
        },
        label = { Text(tab.options.title, fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Primary,
            selectedTextColor = Primary,
            indicatorColor = Primary.copy(alpha = 0.1f),
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
        Navigator(HomeVoyager)
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
        Navigator(StadiumVoyager)
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
        Navigator(CoachesVoyager)
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
