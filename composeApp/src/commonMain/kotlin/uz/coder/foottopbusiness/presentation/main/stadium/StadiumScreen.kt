@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.stadium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.main.stadium.tabs.DetailsTabContent
import uz.coder.foottopbusiness.presentation.main.stadium.tabs.ImagesTabContent
import uz.coder.foottopbusiness.presentation.main.stadium.tabs.PricingTabContent
import uz.coder.foottopbusiness.presentation.main.stadium.tabs.StadiumTab

@Composable
fun StadiumScreen(viewModel: StadiumViewModel, onNavigateToAddPitch: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tabs = StadiumTab.values()
    val selectedIndex = tabs.indexOf(state.selectedTab)

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedIndex,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedIndex])
                        .height(2.dp)
                        .background(Primary)
                )
            },
            divider = { HorizontalDivider() }
        ) {
            tabs.forEach { tab ->
                val selected = state.selectedTab == tab
                Tab(
                    selected = selected,
                    onClick = { viewModel.handleEvent(StadiumContract.Event.SelectTab(tab)) },
                    text = {
                        Text(
                            text = tab.label,
                            color = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (state.selectedTab) {
            StadiumTab.Details -> DetailsTabContent(state) { viewModel.handleEvent(it) }
            StadiumTab.Pricing -> PricingTabContent(onAddPitch = onNavigateToAddPitch)
            StadiumTab.Images -> ImagesTabContent()
        }
    }
}
