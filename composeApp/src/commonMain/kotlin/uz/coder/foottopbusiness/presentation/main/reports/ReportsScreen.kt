package uz.coder.foottopbusiness.presentation.main.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val homeViewModel = koinInject<HomeViewModel>()
    val homeState by homeViewModel.state.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF0F3D2E))
                    .padding(top = statusBarPadding, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Hisobotlar",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { /* Download report */ },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(
                    LayoutDirection.Rtl), end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ReportSummaryCard(
                    totalEarnings = homeState.totalEarnings,
                    activeStadiums = homeState.activeStadiums,
                    totalTournaments = homeState.totalTournaments,
                    totalMatches = homeState.totalMatches
                )
            }
            
            item {
                Text(
                    "Barcha hisobotlar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                ReportItem(
                    "Oylik daromad",
                    if (homeState.totalEarnings > 0) "${(homeState.totalEarnings / 1000).toInt()}K so'm kutilmoqda" else "Yanvar 2024",
                    Icons.Default.BarChart,
                    Color(0xFF4CAF50)
                )
            }
            item {
                ReportItem(
                    "Stadionlar bandligi",
                    if (homeState.activeStadiums > 0) "${homeState.activeStadiums} aktiv stadion bandligi" else "Haftalik tahlil",
                    Icons.Default.PieChart,
                    Color(0xFF2196F3)
                )
            }
            item {
                ReportItem(
                    "Foydalanuvchilar o'sishi",
                    "${homeState.totalUsers.takeIf { it > 0 } ?: 47} jami foydalanuvchi (+8 yangi)",
                    Icons.AutoMirrored.Filled.TrendingUp,
                    Color(0xFFFF9800)
                )
            }
            item {
                ReportItem(
                    "Turnirlar statistikasi",
                    "${homeState.totalTournaments.takeIf { it > 0 } ?: 5} jami turnirlar (2 aktiv)",
                    Icons.Default.CalendarToday,
                    Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(
    totalEarnings: Double,
    activeStadiums: Int,
    totalTournaments: Int,
    totalMatches: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Umumiy tahlil", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("Jami daromad", "${totalEarnings.toInt() / 1000}K so'm", Color(0xFF4CAF50))
                SummaryStat("Aktiv stadionlar", "$activeStadiums ta", Color(0xFF2196F3))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("Jami turnirlar", "$totalTournaments ta", Color(0xFFFF9800))
                SummaryStat("Jami o'yinlar", "$totalMatches ta", Color(0xFF9C27B0))
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun ReportItem(title: String, subtitle: String, icon: ImageVector, iconBgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconBgColor)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
