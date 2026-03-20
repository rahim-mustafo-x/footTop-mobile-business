package uz.coder.foottopbusiness.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesVoyager
import uz.coder.foottopbusiness.presentation.main.home.HomeVoyager
import uz.coder.foottopbusiness.presentation.main.settings.SettingsVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchScreen
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchViewModel
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsVoyager

private data class TabItem(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit,
)

@Composable
fun MainScreen() {
    val addPitchViewModel = koinInject<AddPitchViewModel>()
    var showAddPitch by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            TabItem("Bosh sahifa", Icons.Default.Home) { HomeVoyager.Content() },
            TabItem("Stadion", Icons.Default.Place) { StadiumVoyager.ContentWithNav(onNavigateToAddPitch = { showAddPitch = true }) },
            TabItem("Murabbiylar", Icons.Default.Person) { CoachesVoyager.Content() },
            TabItem("Turnirlar", Icons.Default.DateRange) { TournamentsVoyager.Content() },
            TabItem("Sozlamalar", Icons.Default.Settings) { SettingsVoyager.Content() },
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }

    BackHandler(enabled = !showAddPitch && selectedIndex != 0) {
        selectedIndex = 0
    }

    if (showAddPitch) {
        AddPitchScreen(
            viewModel = addPitchViewModel,
            onBack = { showAddPitch = false }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = Primary) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White.copy(alpha = 0.2f),
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            tabs[selectedIndex].content()
        }
    }
}
