package uz.coder.foottopbusiness.presentation.main.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import uz.coder.foottopbusiness.core.Money
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Language
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.platform.openFile
import uz.coder.foottopbusiness.core.saveFile
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.ui.HeaderIconButton
import uz.coder.foottopbusiness.data.network.dto.owner.OwnerReportDto
import uz.coder.foottopbusiness.presentation.main.booking.details.BookingDetailsScreen
import uz.coder.foottopbusiness.presentation.main.components.BookingRowItem

/** "2026-10-23" -> "23.10.2026" */
private fun dmy(iso: String): String = iso.take(10).split('-').reversed().joinToString(".")

/** 12.5 -> "12,5%" */
private fun percent(v: Double?): String = v?.let { "${trim(it)}%" } ?: "—"

private fun trim(v: Double): String {
    val r = kotlin.math.round(v * 10) / 10
    return (if (r == kotlin.math.floor(r)) r.toLong().toString() else r.toString()).replace('.', ',')
}

private fun hours(strings: Language, h: Double) = "${trim(h)} ${strings.hoursUnit}"

@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = Localization.current
    // Hisobot ba'zi rollarda navigatorsiz tabda turadi - u holda bron tafsiloti ochilmaydi
    val navigator = LocalNavigator.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportsContract.Effect.DownloadFile -> {
                    val filePath = saveFile(effect.fileName, effect.content)
                    when {
                        filePath == null -> snackbarHostState.showSnackbar(strings.fileSaveError)
                        // Fayl saqlandi, lekin uni ochadigan ilova topilmadi -
                        // foydalanuvchiga hech bo'lmasa yo'lni aytamiz
                        !openFile(filePath) -> snackbarHostState.showSnackbar("${strings.fileSaved}: $filePath")
                    }
                }
                ReportsContract.Effect.PaymentUpdated -> snackbarHostState.showSnackbar(strings.paymentStatusUpdated)
                is ReportsContract.Effect.ShowError -> snackbarHostState.showSnackbar(
                    effect.message.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) }
                        ?: strings.unknownNetworkError
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GradientHeader(
                title = strings.financialReport,
                subtitle = if (state.from.isNotEmpty()) "${dmy(state.from)} – ${dmy(state.to)}" else null,
                actions = {
                    HeaderIconButton(
                        icon = if (state.isExporting) Icons.Default.HourglassEmpty else Icons.Default.Download,
                        onClick = { viewModel.handleEvent(ReportsContract.Event.DownloadReport) },
                        contentDescription = strings.download
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ReportPeriod.WEEK to strings.period7,
                    ReportPeriod.DAYS_30 to strings.period30,
                    ReportPeriod.MONTH to strings.periodMonth
                ).forEach { (p, label) ->
                    FilterChip(
                        selected = state.period == p,
                        onClick = { viewModel.handleEvent(ReportsContract.Event.SelectPeriod(p)) },
                        label = { Text(label) }
                    )
                }
            }

            val r = state.report
            when {
                state.isLoading && r == null -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && r == null -> Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudOff, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        state.error?.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) } ?: strings.loadFailed,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = { viewModel.handleEvent(ReportsContract.Event.Load) }) { Text(strings.retry) }
                }
                r != null -> ReportContent(
                    r = r,
                    state = state,
                    strings = strings,
                    onOpenBooking = navigator?.let { nav -> { b -> nav.push(BookingDetailsScreen(b)) } },
                    onMarkPaid = { viewModel.handleEvent(ReportsContract.Event.MarkPaid(it)) }
                )
            }
        }
    }
}

@Composable
private fun ReportContent(
    r: OwnerReportDto,
    state: ReportsContract.State,
    strings: Language,
    onOpenBooking: ((uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto) -> Unit)?,
    onMarkPaid: (Long) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    // Tushum kartasi
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(primary, primary.copy(alpha = 0.75f))),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.width(6.dp))
                Text(strings.revenue, color = Color.White.copy(alpha = 0.85f))
            }
            Text(
                Money.compactWithCurrency(r.revenue, strings.currency),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                IncomeStat(strings.paidLabel, Money.compactWithCurrency(r.paidAmount, strings.currency), Modifier.weight(1f))
                IncomeStat(strings.unpaidLabel, Money.compactWithCurrency(r.unpaidAmount, strings.currency), Modifier.weight(1f))
                IncomeStat(strings.occupancy, percent(r.occupancyPercent), Modifier.weight(1f))
            }
        }
    }

    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatChip(strings.confirmedShort, r.confirmedCount.toString())
        StatChip(strings.statusPending, r.pendingCount.toString())
        StatChip(strings.cancelledShort, (r.cancelledCount + r.rejectedCount).toString())
        StatChip(strings.noShow, r.noShowCount.toString())
        StatChip(null, hours(strings, r.bookedHours))
    }

    SectionTitle(strings.debtors)
    if (r.unpaid.isEmpty()) {
        EmptyCard(strings.noDebtors)
    } else {
        Text(strings.debtorsHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        r.unpaid.forEach { b ->
            BookingRowItem(
                booking = b,
                title = b.name?.takeIf { it.isNotBlank() } ?: b.stadiumName,
                onClick = { onOpenBooking?.invoke(b) },
                action = {
                    val id = b.id
                    if (id != null) {
                        FilledTonalButton(
                            onClick = { onMarkPaid(id) },
                            enabled = state.payingId == null,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (state.payingId == id) {
                                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(strings.paidLabel, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        }
    }

    SectionTitle(strings.dailyRevenue)
    BarChart(
        bars = r.days.map { Bar(it.date, dmy(it.date), it.date.takeLast(2), it.revenue) },
        format = { Money.withCurrency(it, strings.currency) },
        emptyText = strings.noDataYet
    )

    SectionTitle(strings.peakHours)
    // Faqat o'yin bo'lgan soatlar oralig'i - telefonda ustunlar o'qiladigan bo'lsin
    val used = r.hours.filter { it.bookings > 0 }.map { it.hour }
    val shownHours = if (used.isEmpty()) emptyList() else r.hours.filter { it.hour in used.min()..used.max() }
    BarChart(
        bars = shownHours.map {
            val hh = it.hour.toString().padStart(2, '0')
            Bar(hh, "$hh:00", hh, it.bookings.toDouble())
        },
        format = { strings.bookingCount(it.toInt()) },
        emptyText = strings.noDataYet
    )

    SectionTitle(strings.byPitch)
    r.stadiums.forEach { s ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        s.stadiumName.orEmpty(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(Money.withCurrency(s.revenue, strings.currency), fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { ((s.occupancyPercent ?: 0.0) / 100.0).coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    drawStopIndicator = {}
                )
                Text(
                    "${strings.occupancy}: ${s.occupancyPercent?.let(::percent) ?: strings.noOccupancyData} · " +
                        "${strings.bookingCount(s.bookings)} · ${hours(strings, s.bookedHours)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IncomeStat(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), maxLines = 1)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun StatChip(label: String?, value: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(999.dp)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (label != null) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
            }
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(text, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class Bar(val key: String, val label: String, val tick: String, val value: Double)

/** Bitta qatorli ustunli grafik: ustunni bossangiz qiymati yuqorida chiqadi. */
@Composable
private fun BarChart(bars: List<Bar>, format: (Double) -> String, emptyText: String) {
    val max = bars.maxOfOrNull { it.value } ?: 0.0
    if (max <= 0.0) {
        EmptyCard(emptyText)
        return
    }
    var active by remember(bars) { mutableStateOf<String?>(null) }
    val shown = bars.firstOrNull { it.key == active }
    // Har N-chi belgini yozamiz - bir-birining ustiga chiqmasin
    val every = maxOf(1, (bars.size + 7) / 8)
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                if (shown != null) {
                    Text("${shown.label}: ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text(format(shown.value), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("max ${format(max)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { b ->
                    Box(
                        Modifier.weight(1f).fillMaxHeight().clickable { active = b.key },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (b.value > 0) {
                            Box(
                                Modifier
                                    .fillMaxWidth(0.7f)
                                    .fillMaxHeight(maxOf(0.02f, (b.value / max).toFloat()))
                                    .background(
                                        if (b.key == active) primary else primary.copy(alpha = 0.7f),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                bars.forEachIndexed { i, b ->
                    Text(
                        if (i % every == 0) b.tick else "",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
