package uz.coder.foottopbusiness.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val addPitchViewModel = koinInject<AddPitchViewModel>()
    var showAddPitch by remember { mutableStateOf(false) }

    val drawerItems = remember {
        listOf(
            DrawerItem("Bosh sahifa", Icons.Default.Home) { HomeVoyager.Content() },
            DrawerItem("Manage Stadium", Icons.Default.Place) { StadiumVoyager.ContentWithNav(onNavigateToAddPitch = { showAddPitch = true }) },
            DrawerItem("Coaches", Icons.Default.Person) { CoachesVoyager.Content() },
            DrawerItem("Tournaments", Icons.Default.DateRange) { TournamentsVoyager.Content() },
            DrawerItem("Sozlamalar", Icons.Default.Settings) { SettingsVoyager.Content() },
        )
    }

    var selectedItem by remember { mutableStateOf(drawerItems.first()) }

    // BackHandler logic: If not on Home, go to Home. If on Home, let system exit.
    BackHandler(enabled = !showAddPitch && (drawerState.isOpen || selectedItem != drawerItems.first())) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            selectedItem = drawerItems.first()
        }
    }

    if (showAddPitch) {
        AddPitchScreen(
            viewModel = addPitchViewModel,
            onBack = { showAddPitch = false }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerTonalElevation = 0.dp
                ) {
                    Spacer(Modifier.height(24.dp))

                    val title = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) { append("Foot") }
                        withStyle(SpanStyle(color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)) { append("Top") }
                    }
                    Text(title, modifier = Modifier.padding(horizontal = 24.dp))
                    Text(
                        "Business",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label, fontWeight = if (selectedItem.label == item.label) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            selected = selectedItem.label == item.label,
                            onClick = {
                                selectedItem = item
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Primary.copy(alpha = 0.1f),
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(selectedItem.label, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    selectedItem.content()
                }
            }
        }
    }
}
