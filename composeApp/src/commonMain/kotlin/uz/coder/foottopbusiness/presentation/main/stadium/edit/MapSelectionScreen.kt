package uz.coder.foottopbusiness.presentation.main.stadium.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.ui.MapView

class MapSelectionScreen(
    private val initialLatitude: Double?,
    private val initialLongitude: Double?,
    private val onSelected: (Double, Double) -> Unit
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selectedLat by remember { mutableStateOf(initialLatitude) }
        var selectedLng by remember { mutableStateOf(initialLongitude) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Joylashuvni tanlang") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                if (selectedLat != null && selectedLng != null) {
                    FloatingActionButton(
                        onClick = {
                            onSelected(selectedLat!!, selectedLng!!)
                            navigator.pop()
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm")
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    initialLatitude = selectedLat,
                    initialLongitude = selectedLng,
                    onLocationSelected = { lat, lng ->
                        selectedLat = lat
                        selectedLng = lng
                    }
                )
            }
        }
    }
}
