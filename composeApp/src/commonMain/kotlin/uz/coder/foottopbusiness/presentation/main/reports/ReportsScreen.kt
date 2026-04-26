package uz.coder.foottopbusiness.presentation.main.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val homeViewModel = koinInject<HomeViewModel>()
    val homeState by homeViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        homeViewModel.effect.collect { effect ->
            when (effect) {
                is HomeContract.Effect.DownloadFile -> {
                    val filePath = uz.coder.foottopbusiness.core.saveFile(effect.fileName, effect.content)
                    if (filePath != null) {
                        uz.coder.foottopbusiness.core.platform.openFile(filePath)
                    } else {
                        snackbarHostState.showSnackbar("Faylni saqlashda xatolik")
                    }
                    homeViewModel.handleEvent(HomeContract.Event.Load)
                }
                is HomeContract.Effect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true
                    )
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
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
                            "Moliyaviy Hisobot",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Daromad va tahlillar monitoringi",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { homeViewModel.handleEvent(HomeContract.Event.DownloadReport) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                homeState.weeklyReport?.let { report ->
                    IncomeOverviewCard(
                        totalEarnings = report.weeklyRevenue,
                        weeklyTotal = report.weeklyRevenue,
                        totalUsers = homeState.dashboard?.usersCount ?: 0,
                        growth = report.bookingsGrowthPercent
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                WeeklyRevenueChart(homeState.weeklyEarnings, homeState.weeklyLabels)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Kunlik tafsilotlar",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { /* TODO */ }) {
                        Text(
                            "Filtrlash",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val recentMatches = homeState.matches
                .filter { it.dateTime != null }
                .sortedByDescending { it.dateTime }
                .take(7)

            items(recentMatches) { match ->
                val datePart = match.dateTime?.split("T")?.firstOrNull() ?: ""
                val total = (match.currentPlayers ?: 0) * (match.pricePerPlayer ?: 0.0)

                ReportItem(
                    title = match.title ?: "O'yin",
                    subtitle = "$datePart • ${match.currentPlayers} ta bandlar • ${total.toInt()} so'm",
                    icon = Icons.Default.BarChart,
                    iconBgColor = MaterialTheme.colorScheme.primary
                )
            }

            if (recentMatches.isEmpty()) {
                item {
                    Text(
                        "Hozircha ma'lumotlar yo'q",
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeOverviewCard(totalEarnings: Double, weeklyTotal: Double, totalUsers: Int, growth: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Pattern or Gradient overlay
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width / 2,
                    center = Offset(size.width * 0.9f, 0f)
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChart, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("UMUMIY HISOB", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                }
                Spacer(Modifier.height(8.dp))
                val formattedEarnings = if (totalEarnings > 1000000) {
                    "${(totalEarnings / 1000000).toInt()}M UZS"
                } else {
                    "${totalEarnings.toInt() / 1000}K UZS"
                }
                Text(formattedEarnings, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.1f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val weeklyFormatted = if (weeklyTotal > 1000000) "${(weeklyTotal / 1000000).toInt()}M" else "${(weeklyTotal / 1000).toInt()}K"
                    SummaryStatSmall("BU HAFTA", weeklyFormatted, Color.White)
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.2f)))
                    SummaryStatSmall("O'SISH", "${if(growth >= 0) "+" else ""}$growth%", Color(0xFFB9F6CA))
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.2f)))
                    SummaryStatSmall("MIJOZLAR", totalUsers.toString(), Color.White)
                }
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
private fun WeeklyRevenueChart(
    weeklyEarnings: List<Double>,
    weeklyLabels: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Haftalik tahlil", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(
                        "Daromad dinamikasi",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            val data = if (weeklyEarnings.isNotEmpty()) {
                val max = weeklyEarnings.maxOrNull() ?: 1.0
                weeklyEarnings.map { (it / max).toFloat().coerceIn(0.1f, 1f) }
            } else {
                listOf(0.3f, 0.5f, 0.4f, 0.8f, 0.6f, 0.9f, 0.7f)
            }
            
            val days = weeklyLabels.ifEmpty { listOf("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya") }
            val primaryColor = MaterialTheme.colorScheme.primary

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (data.size - 1)

                    val path = Path()
                    val fillPath = Path()

                    data.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = height - (value * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * spacing
                            val prevY = height - (data[index - 1] * height)

                            // Smooth curve using cubic bezier
                            val controlX1 = prevX + spacing / 2
                            val controlX2 = x - spacing / 2

                            path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                            fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Draw gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f),
                                primaryColor.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw the line
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw points
                    data.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = height - (value * height)

                        // Outer circle (glow)
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.2f),
                            radius = 8.dp.toPx(),
                            center = Offset(x, y)
                        )
                        // Middle circle
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                        // Inner circle (center)
                        drawCircle(
                            color = primaryColor,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { index, day ->
                    Text(
                        day,
                        fontSize = 12.sp,
                        fontWeight = if (index == 5 || index == 6) FontWeight.Bold else FontWeight.Medium,
                        color = if (index == 5 || index == 6) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
