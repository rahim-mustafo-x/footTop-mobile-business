package uz.coder.foottopbusiness.presentation.main.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.material3.MaterialTheme
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(top = statusBarPadding, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (homeState.isOwner) "Daromad" else "Hisobotlar",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Tizim faoliyati tahlili",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                    IconButton(
                        onClick = { /* Download report */ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(
                    LayoutDirection.Ltr), end = padding.calculateEndPadding(LayoutDirection.Rtl))
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (homeState.isOwner) {
                item {
                    IncomeOverviewCard(homeState.totalEarnings)
                }
                item {
                    WeeklyRevenueChart()
                }
            } else {
                item {
                    ReportSummaryCard(
                        totalEarnings = homeState.totalEarnings,
                        activeStadiums = homeState.activeStadiums,
                        totalTournaments = homeState.totalTournaments,
                        totalMatches = homeState.totalMatches
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (homeState.isOwner) "So'nggi harakatlar" else "Barcha hisobotlar",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Hammasi",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (homeState.isOwner) {
                items(5) { index ->
                    val days = listOf("Dushanba", "Yakshanba", "Shanba", "Juma", "Payshanba")
                    val amounts = listOf("850,000", "1,200,000", "980,000", "750,000", "600,000")
                    ReportItem(
                        "${days[index % 5]}, ${22 - index} Yanvar",
                        "${amounts[index % 5]} so'm • ${10 - index} bandlar",
                        Icons.Default.BarChart,
                        Color(0xFF26A69A)
                    )
                }
            } else {
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
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun IncomeOverviewCard(totalEarnings: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF26A69A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White)
                )
                Spacer(Modifier.width(8.dp))
                Text("UMUMIY DAROMAD", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(12.dp))
            val formattedEarnings = if (totalEarnings > 1000000) {
                "${(totalEarnings / 1000000).toInt()}M UZS"
            } else {
                "${totalEarnings.toInt()} UZS"
            }
            Text(formattedEarnings, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStatSmall("BU HAFTA", "2.1M", Color.White)
                SummaryStatSmall("O'SISH", "+15.4%", Color(0xFFE0F2F1))
                SummaryStatSmall("BANDLIK", "84%", Color.White)
            }
        }
    }
}

@Composable
private fun SummaryStatSmall(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun WeeklyRevenueChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Haftalik tahlil", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(28.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val data = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.4f)
                val days = listOf("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya")
                
                data.forEachIndexed { index, value ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .fillMaxHeight(value)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (index == 3) Color(0xFF26A69A) 
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(days[index], fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Umumiy tahlil", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("Jami daromad", "${totalEarnings.toInt() / 1000}K so'm", MaterialTheme.colorScheme.primary)
                SummaryStat("Aktiv stadionlar", "$activeStadiums ta", MaterialTheme.colorScheme.secondary)
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
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ReportItem(title: String, subtitle: String, icon: ImageVector, iconBgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
        }
    }
}
