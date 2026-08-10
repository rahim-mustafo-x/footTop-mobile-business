package uz.coder.foottopbusiness.presentation.main.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.Money
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.ui.HeaderIconButton
import uz.coder.foottopbusiness.core.ui.shimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val viewModel = koinInject<ReportsViewModel>()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = Localization.current

    var showDatePicker by remember { mutableStateOf(value = false) }
    val datePickerState = rememberDatePickerState()
    var selectedFilterDate by remember { mutableStateOf<String?>(value = null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportsContract.Effect.DownloadFile -> {
                    val filePath = uz.coder.foottopbusiness.core.saveFile(effect.fileName, effect.content)
                    when {
                        filePath == null -> snackbarHostState.showSnackbar(strings.fileSaveError)
                        // Fayl saqlandi, lekin uni ochadigan ilova topilmadi -
                        // foydalanuvchiga hech bo'lmasa yo'lni aytamiz
                        !uz.coder.foottopbusiness.core.platform.openFile(filePath) ->
                            snackbarHostState.showSnackbar("${strings.fileSaved}: $filePath")
                    }
                }
                is ReportsContract.Effect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = ErrorMapper.map(effect.message, strings),
                        withDismissAction = true
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GradientHeader(
                title = strings.financialReport,
                subtitle = strings.incomeMonitoring,
                actions = {
                    HeaderIconButton(
                        icon = Icons.Default.Download,
                        onClick = { viewModel.handleEvent(ReportsContract.Event.DownloadReport) },
                        contentDescription = strings.download
                    )
                }
            )
        }
    ) { padding ->
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedFilterDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date.toString()
                        }
                        showDatePicker = false
                    }) {
                        Text(strings.save)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        selectedFilterDate = null
                        showDatePicker = false 
                    }) {
                        Text(strings.cancel)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        if (state.isLoadingDashboard || state.isLoadingWeeklyReport) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp, top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(28.dp)).shimmer())
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp)).shimmer())
                }
                item {
                    Box(modifier = Modifier.width(150.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                }
                items(3) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                }
            }
        } else {
            val currentFilterDate = selectedFilterDate
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(
                        LayoutDirection.Ltr), end = padding.calculateRightPadding(LayoutDirection.Rtl))
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp, top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    if (state.weeklyReport != null) {
                        state.weeklyReport?.let { report ->
                            IncomeOverviewCard(
                                totalEarnings = report.weeklyRevenue,
                                weeklyTotal = report.weeklyRevenue,
                                totalUsers = state.dashboard?.usersCount ?: 0,
                                growth = report.bookingsGrowthPercent
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(28.dp)).shimmer())
                    }
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.dailyDetails,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(
                                currentFilterDate ?: strings.filter,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val recentMatches = state.matches
                    .filter { match ->
                        val matchDate = match.dateTime
                        matchDate != null && (currentFilterDate == null || matchDate.startsWith(currentFilterDate))
                    }
                    .sortedByDescending { it.dateTime }

                if (recentMatches.isEmpty() && !state.isLoadingMatches) {
                    item {
                        Text(
                            strings.noDataYet,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(recentMatches) { match ->
                        val datePart = match.dateTime?.split("T")?.firstOrNull() ?: ""
                        val total = (match.currentPlayers ?: 0) * (match.pricePerPlayer ?: 0.0)

                        ReportItem(
                            title = match.title ?: "O'yin",
                            subtitle = "$datePart • ${match.currentPlayers} • " +
                                Money.withCurrency(total, strings.currency),
                            icon = Icons.Default.BarChart,
                            iconBgColor = MaterialTheme.colorScheme.primary,
                            onClick = { /* Handle click */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeOverviewCard(totalEarnings: Double, weeklyTotal: Double, totalUsers: Int, growth: Double) {
    val strings = Localization.current
    // Karta foni primary bo'lgani uchun matn onPrimary bo'lishi shart:
    // qorong'i temada primary och ko'k, unda oq matn o'qilmaydi
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, null, tint = onPrimary.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.incomeOverview, color = onPrimary.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                Money.compactWithCurrency(totalEarnings, strings.currency),
                color = onPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.1f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStatSmall(strings.active.uppercase(), Money.compact(weeklyTotal), onPrimary) // Placeholder for "THIS WEEK"
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(onPrimary.copy(alpha = 0.2f)))
                SummaryStatSmall(strings.growth, "${if(growth >= 0) "+" else ""}$growth%", Color(0xFFB9F6CA))
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(onPrimary.copy(alpha = 0.2f)))
                SummaryStatSmall(strings.customers, totalUsers.toString(), onPrimary)
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
    val strings = Localization.current
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
                    Text(strings.weeklyAnalysis, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(
                        strings.revenueDynamics,
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
                emptyList<Float>()
            }
            
            if (data.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).shimmer())
            } else {
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
}

@Composable
fun ReportItem(title: String, subtitle: String, icon: ImageVector, iconBgColor: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
