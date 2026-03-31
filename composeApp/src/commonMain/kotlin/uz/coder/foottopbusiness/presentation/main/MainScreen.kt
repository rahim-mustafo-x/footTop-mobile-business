package uz.coder.foottopbusiness.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesVoyager
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel
import uz.coder.foottopbusiness.presentation.main.home.HomeVoyager
import uz.coder.foottopbusiness.presentation.main.settings.SettingsVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchScreen
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchViewModel
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsVoyager

private data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val addPitchViewModel = koinInject<AddPitchViewModel>()
    val homeViewModel = koinInject<HomeViewModel>()
    val homeState by homeViewModel.state.collectAsState()
    
    var showAddPitch by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val drawerItems = remember {
        listOf(
            DrawerItem("Bosh sahifa", Icons.Default.Home) { HomeVoyager.Content() },
            DrawerItem("Stadion", Icons.Default.Place) { StadiumVoyager.ContentWithNav(onNavigateToAddPitch = { showAddPitch = true }) },
            DrawerItem("Murabbiylar", Icons.Default.Person) { CoachesVoyager.Content() },
            DrawerItem("Turnirlar", Icons.Default.DateRange) { TournamentsVoyager.Content() },
            DrawerItem("Sozlamalar", Icons.Default.Settings) { SettingsVoyager.Content() },
        )
    }

    var selectedDrawerIndex by remember { mutableStateOf(0) }

    BackHandler(enabled = !showAddPitch) {
        when {
            drawerState.currentValue == DrawerValue.Open -> scope.launch { drawerState.close() }
            selectedDrawerIndex != 0 -> {
                selectedDrawerIndex = 0
                homeViewModel.handleEvent(HomeContract.Event.ChangeTab(0))
            }
            homeState.currentTab != 0 -> homeViewModel.handleEvent(HomeContract.Event.ChangeTab(0))
        }
    }

    if (showAddPitch) {
        AddPitchScreen(
            viewModel = addPitchViewModel,
            onBack = { showAddPitch = !showAddPitch }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp).fillMaxHeight(),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    RowDrawerHeader()
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    drawerItems.forEachIndexed { index, item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = {
                                Text(
                                    item.label,
                                    fontWeight = if (selectedDrawerIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            selected = selectedDrawerIndex == index,
                            onClick = {
                                selectedDrawerIndex = index
                                // Agar Bosh sahifaga o'tilsa, uning tabini 0 (Home) ga qaytarish
                                if (index == 0) homeViewModel.handleEvent(HomeContract.Event.ChangeTab(0))
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Primary.copy(alpha = 0.14f),
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (selectedDrawerIndex == 0) {
                                if (homeState.currentTab == 0) "Bosh sahifa" else "Tarix"
                            } else {
                                drawerItems[selectedDrawerIndex].label
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menyu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
                )
            },
            bottomBar = {
                // BottomBar faqat Bosh sahifa (0-index) tanlangan bo'lsa ko'rinadi
                if (selectedDrawerIndex == 0) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = homeState.currentTab == 0,
                            onClick = { homeViewModel.handleEvent(HomeContract.Event.ChangeTab(0)) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Bosh sahifa") },
                            label = { Text("Bosh sahifa", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Primary.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                        NavigationBarItem(
                            selected = homeState.currentTab == 1,
                            onClick = { homeViewModel.handleEvent(HomeContract.Event.ChangeTab(1)) },
                            icon = { Icon(Icons.Default.History, contentDescription = "Tarix") },
                            label = { Text("Tarix", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Primary.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Bu yerda DRAWER orqali tanlangan kontent ko'rsatiladi
                // Agar index 0 bo'lsa, HomeVoyager ichidagi HomeScreen state.currentTab ga qarab Home yoki Historyni ko'rsatadi
                drawerItems[selectedDrawerIndex].content()
            }
        }
    }
}

@Composable
private fun RowDrawerHeader() {
    Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.SportsSoccer,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.padding(end = 12.dp),
        )
        Column {
            Text(
                "FootTop Business",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Boshqaruv paneli",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
