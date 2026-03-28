package uz.coder.foottopbusiness.presentation.main.home.slots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.formatAsDate
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotsControlScreen(stadium: StadiumResponse, state: HomeContract.State, viewModel: HomeViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stadium.name ?: "Slotlar") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.handleEvent(HomeContract.Event.ClearStadiumForSlots)
                        navigator.pop()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listOf("2024-05-20", "2024-05-21", "2024-05-22", "2024-05-23", "2024-05-24")) { date ->
                    val isSelected = state.selectedDate == date
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Primary else Color.White)
                            .border(1.dp, if (isSelected) Primary else Color.LightGray, RoundedCornerShape(12.dp))
                            .clickable { viewModel.handleEvent(HomeContract.Event.ChangeDate(date)) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(date.takeLast(2), color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Vaqt Slotlari", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            
            if (state.isLoadingSlots) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.stadiumSlots) { (start, _, available) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (available) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                .border(1.dp, if (available) Color(0xFF4CAF50) else Color(0xFFF44336), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(start.formatAsDate(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(start.formatAsTime(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(if (available) "Bo'sh" else "Band", fontSize = 10.sp, color = if (available) Color(0xFF4CAF50) else Color(0xFFF44336))
                            }
                        }
                    }
                }
            }
        }
    }
}
