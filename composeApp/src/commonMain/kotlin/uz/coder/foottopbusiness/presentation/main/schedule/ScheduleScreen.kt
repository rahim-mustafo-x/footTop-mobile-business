package uz.coder.foottopbusiness.presentation.main.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toInstant
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Language
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.Error
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.ui.Success
import uz.coder.foottopbusiness.core.ui.TimeStepper
import uz.coder.foottopbusiness.core.ui.Warning
import uz.coder.foottopbusiness.core.visualTransformation.PhoneTransformation
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.presentation.main.booking.components.BookingOptionsSection
import uz.coder.foottopbusiness.presentation.main.booking.details.BookingDetailsScreen
import uz.coder.foottopbusiness.presentation.main.home.slots.durationMinutes
import kotlin.time.Clock

/** Bir soat qatorining balandligi: 60 daqiqalik bronga ism va vaqt sig'adi. */
private val HOUR_H = 64.dp
private val TIME_COL = 48.dp
private val MIN_LANE = 140.dp
private val DURATIONS = listOf("SIXTY", "NINETY", "ONE_HUNDRED_TWENTY")

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = Localization.current
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = remember { SnackbarHostState() }

    // "Hozir" chizig'i va o'tgan soatlar har daqiqada yangilanadi
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000 - Clock.System.now().toEpochMilliseconds() % 60_000)
            tick++
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            val message = when (effect) {
                is ScheduleContract.Effect.BookingCreated -> "${strings.bookingCreated}: ${effect.name}"
                ScheduleContract.Effect.BlockCreated -> strings.blockCreated
                ScheduleContract.Effect.BlockDeleted -> strings.blockDeleted
                is ScheduleContract.Effect.ShowError ->
                    effect.message.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) }
                        ?: strings.unknownNetworkError
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                GradientHeader(title = strings.scheduleTitle, subtitle = strings.longDate(state.date))
                DayStrip(state, strings) { viewModel.handleEvent(ScheduleContract.Event.SelectDate(it)) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                state.isLoading && state.lanes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && state.lanes.isEmpty() -> Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        state.error?.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) } ?: strings.loadFailed,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = { viewModel.handleEvent(ScheduleContract.Event.Refresh) }) { Text(strings.retry) }
                }
                state.lanes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(strings.noStadiumsYet, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    Summary(state, strings)
                    ScheduleGrid(
                        state = state,
                        tick = tick,
                        modifier = Modifier.weight(1f),
                        onTap = { lane, minute -> viewModel.handleEvent(ScheduleContract.Event.TapLane(lane, minute)) },
                        onBooking = { navigator.push(BookingDetailsScreen(it)) },
                        onBlock = { viewModel.handleEvent(ScheduleContract.Event.OpenUnblock(it.block)) }
                    )
                    Legend(strings)
                }
            }
        }
    }

    PickDialog(state, viewModel, strings)
    BookingDialog(state, viewModel, strings)
    BlockDialog(state, viewModel, strings)
    UnblockDialog(state, viewModel, strings)
}

@Composable
private fun DayStrip(state: ScheduleContract.State, strings: Language, onSelect: (kotlinx.datetime.LocalDate) -> Unit) {
    val weekdays = strings.weekdaysShort.split(',')
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.days.forEachIndexed { i, d ->
            val on = d == state.date
            Surface(
                onClick = { onSelect(d) },
                shape = RoundedCornerShape(14.dp),
                color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.width(62.dp)
            ) {
                Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        d.day.toString(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        when (i) {
                            0 -> strings.today
                            1 -> strings.tomorrow
                            else -> weekdays.getOrElse(d.dayOfWeek.isoDayNumber - 1) { "" }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Summary(state: ScheduleContract.State, strings: Language) {
    val pending = state.bookings.count { it.status == "PENDING" }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CountChip("${state.bookings.size}", strings.schedBookingsUnit, MaterialTheme.colorScheme.primary)
            if (pending > 0) CountChip("$pending", strings.schedPendingUnit, Warning)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text(strings.scheduleHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CountChip(count: String, unit: String, color: Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(count, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.width(4.dp))
            Text(unit, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ScheduleGrid(
    state: ScheduleContract.State,
    tick: Int,
    modifier: Modifier,
    onTap: (laneId: Long, minute: Int) -> Unit,
    onBooking: (BookingResponseDto) -> Unit,
    onBlock: (LaneBlock) -> Unit
) {
    val strings = Localization.current
    val rangeStart = state.rangeStart
    val rangeEnd = state.rangeEnd
    val hours = (rangeStart until rangeEnd step 60).toList()
    val y = { minutes: Int -> HOUR_H * ((minutes - rangeStart) / 60f) }
    val totalH = y(rangeEnd)
    val nowMinute = remember(tick, state.date) { nowMinuteOn(state.date) }
    val nowY = nowMinute?.takeIf { it in rangeStart..rangeEnd }?.let(y)
    val tz = TimeZone.currentSystemDefault()
    val now = remember(tick) { Clock.System.now() }
    val isPast = { endMinute: Int -> state.date.at(endMinute).toInstant(tz) <= now }

    val vScroll = rememberScrollState()
    val density = LocalDensity.current
    // Ochilganda kun qayerda bo'layotgan bo'lsa o'sha yer: bugun - hozir, aks holda birinchi bron
    LaunchedEffect(state.date, state.isLoading) {
        if (state.isLoading) return@LaunchedEffect
        val firstStart = state.lanes.flatMap { bookingsOf(it, state.date, state.bookings) }.minOfOrNull { it.start }
        val focus = nowY?.takeIf { it > 0.dp } ?: firstStart?.let(y)
        vScroll.scrollTo(with(density) { (focus?.minus(HOUR_H) ?: 0.dp).coerceAtLeast(0.dp).roundToPx() })
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        BoxWithConstraints {
            val laneW = maxOf(MIN_LANE, (maxWidth - TIME_COL) / state.lanes.size)
            val hScroll = rememberScrollState()
            Column(Modifier.horizontalScroll(hScroll)) {
                // Maydon nomlari
                Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                    Spacer(Modifier.width(TIME_COL))
                    state.lanes.forEach { lane ->
                        Column(
                            Modifier.width(laneW).padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                lane.stadium.name.orEmpty(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${hm(lane.open)}–${hm(lane.close)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Row(Modifier.verticalScroll(vScroll)) {
                    // Soatlar ustuni
                    Box(Modifier.width(TIME_COL).height(totalH)) {
                        hours.forEach { h ->
                            Text(
                                hm(h),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.offset(y = y(h) + 2.dp).padding(start = 6.dp)
                            )
                        }
                        if (nowY != null && nowMinute != null) {
                            Surface(
                                color = Error,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.offset(x = 2.dp, y = nowY - 9.dp)
                            ) {
                                Text(
                                    hm(nowMinute),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    state.lanes.forEach { lane ->
                        LaneColumn(
                            lane = lane,
                            state = state,
                            width = laneW,
                            height = totalH,
                            hours = hours,
                            y = y,
                            nowY = nowY,
                            isPast = isPast,
                            strings = strings,
                            onTap = onTap,
                            onBooking = onBooking,
                            onBlock = onBlock
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LaneColumn(
    lane: Lane,
    state: ScheduleContract.State,
    width: Dp,
    height: Dp,
    hours: List<Int>,
    y: (Int) -> Dp,
    nowY: Dp?,
    isPast: (Int) -> Boolean,
    strings: Language,
    onTap: (Long, Int) -> Unit,
    onBooking: (BookingResponseDto) -> Unit,
    onBlock: (LaneBlock) -> Unit
) {
    val density = LocalDensity.current
    val hourPx = with(density) { HOUR_H.toPx() }
    val rangeStart = state.rangeStart
    val rangeEnd = state.rangeEnd
    val bookings = bookingsOf(lane, state.date, state.bookings)
    val blocks = blocksOf(lane, state.date, state.blocks, rangeStart, rangeEnd)
    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Box(
        Modifier
            .width(width)
            .height(height)
            .border(BorderStroke(0.5.dp, outline))
            .pointerInput(lane.id, rangeStart, state.date) {
                detectTapGestures { offset ->
                    onTap(lane.id, rangeStart + (offset.y / hourPx * 60).toInt())
                }
            }
    ) {
        // Soat chiziqlari
        hours.forEach { h ->
            HorizontalDivider(Modifier.offset(y = y(h)), color = outline)
        }
        // Ish vaqtidan tashqari
        if (lane.open > rangeStart) {
            Box(Modifier.fillMaxWidth().height(y(lane.open)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)))
        }
        if (lane.close < rangeEnd) {
            Box(
                Modifier.fillMaxWidth().offset(y = y(lane.close)).height(y(rangeEnd) - y(lane.close))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            )
        }

        // Butun bo'sh soatlar: kelgusilari "+ Bo'sh", o'tganlari "Bo'sh qolgan"
        hours.filter { h ->
            h >= lane.open && h + 60 <= lane.close && !taken(lane, state.date, state.bookings, state.blocks, h, h + 60)
        }.forEach { h ->
            val past = isPast(h + 60)
            Box(
                Modifier
                    .offset(y = y(h))
                    .fillMaxWidth()
                    .height(HOUR_H)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .then(
                        if (past) Modifier.background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.07f),
                            RoundedCornerShape(8.dp)
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (past) {
                    Text(strings.schedFreePast, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                        Text(
                            strings.schedFree,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }

        if (nowY != null) {
            HorizontalDivider(Modifier.offset(y = nowY), thickness = 2.dp, color = Error)
        }

        blocks.forEach { b ->
            ScheduleChip(
                top = y(b.start),
                height = maxOf(22.dp, y(b.end) - y(b.start)),
                color = Warning,
                title = strings.closedTime,
                lines = listOfNotNull("${hm(b.start)}–${hm(b.end)}", b.block.reason),
                dashed = true,
                onClick = { onBlock(b) }
            )
        }
        bookings.forEach { b ->
            val booking = b.booking
            val color = when {
                booking.noShow == true -> MaterialTheme.colorScheme.onSurfaceVariant
                booking.status == "PENDING" -> Warning
                booking.status == "CONFIRMED" -> Success
                else -> MaterialTheme.colorScheme.primary
            }
            val paid = if (booking.paymentStatus == "PAID") " · ✓" else ""
            ScheduleChip(
                top = y(b.start),
                height = maxOf(22.dp, y(b.end) - y(b.start)),
                color = color,
                title = booking.name?.takeIf { it.isNotBlank() } ?: "—",
                lines = listOf("${hm(b.start)}–${hm(b.end)}$paid"),
                dashed = booking.status == "PENDING",
                onClick = { onBooking(booking) }
            )
        }
    }
}

@Composable
private fun ScheduleChip(
    top: Dp,
    height: Dp,
    color: Color,
    title: String,
    lines: List<String>,
    dashed: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .offset(y = top)
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 3.dp, vertical = 1.dp)
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
            .then(if (dashed) Modifier.border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(10.dp)) else Modifier)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.width(4.dp).fillMaxSize().background(color, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)))
        Column(Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
            lines.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun Legend(strings: Language) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(Success, strings.confirmedShort)
        LegendDot(Warning, strings.statusPending)
        LegendDot(Warning.copy(alpha = 0.4f), strings.closedTime)
        LegendDot(MaterialTheme.colorScheme.onSurfaceVariant, strings.noShow)
        LegendDot(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), strings.schedFreePast)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

/** Bo'sh joy bosildi: davomiylikni tanlab bron qilish yoki vaqtni yopish. */
@Composable
private fun PickDialog(state: ScheduleContract.State, viewModel: ScheduleViewModel, strings: Language) {
    val pick = state.pick ?: return
    val lane = state.lane(pick.laneId) ?: return
    val minutes = durationMinutes(state.durationKey)
    val fits = viewModel.pickFits(state)
    AlertDialog(
        onDismissRequest = { viewModel.handleEvent(ScheduleContract.Event.DismissPick) },
        title = { Text(lane.stadium.name.orEmpty()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.chooseAction, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DURATIONS.forEach { key ->
                        FilterChip(
                            selected = key == state.durationKey,
                            onClick = { viewModel.handleEvent(ScheduleContract.Event.SelectDuration(key)) },
                            label = { Text("${durationMinutes(key)} min", maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Boshlanish va tugash vaqti - tanlangan davomiylikka qarab
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeLabel(strings.timeFrom, hm(pick.start), MaterialTheme.colorScheme.primary, Alignment.Start)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        TimeLabel(
                            strings.timeTo,
                            hm(pick.start + minutes),
                            if (fits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            Alignment.End
                        )
                    }
                }
                if (!fits) {
                    Text(strings.slotNotEnoughForDuration, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.handleEvent(ScheduleContract.Event.StartBooking) }, enabled = fits) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.createBooking)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { viewModel.handleEvent(ScheduleContract.Event.StartBlock) }) {
                Icon(Icons.Default.Block, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.blockTime)
            }
        }
    )
}

@Composable
private fun TimeLabel(label: String, value: String, color: Color, align: Alignment.Horizontal) {
    Column(horizontalAlignment = align) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun BookingDialog(state: ScheduleContract.State, viewModel: ScheduleViewModel, strings: Language) {
    val pick = state.bookingFor ?: return
    val minutes = durationMinutes(state.durationKey)
    val phoneIncomplete = state.bookerPhone.isNotEmpty() && state.bookerPhone.length < 9
    AlertDialog(
        onDismissRequest = { viewModel.handleEvent(ScheduleContract.Event.DismissBooking) },
        title = { Text(strings.bookNow) },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${hm(pick.start)} - ${hm(pick.start + minutes)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "${strings.duration}: $minutes min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedTextField(
                    value = state.bookerName,
                    onValueChange = { viewModel.handleEvent(ScheduleContract.Event.UpdateBookerName(it)) },
                    label = { Text(strings.fullName) },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.bookerPhone,
                    onValueChange = { input ->
                        viewModel.handleEvent(ScheduleContract.Event.UpdateBookerPhone(input.filter { it.isDigit() }.take(9)))
                    },
                    label = { Text(strings.phoneNumber) },
                    prefix = { Text("+998 ") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PhoneTransformation(),
                    isError = phoneIncomplete,
                    supportingText = if (phoneIncomplete) { { Text(strings.enterFullPhone) } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                BookingOptionsSection(
                    options = state.bookingOptions,
                    onChange = { viewModel.handleEvent(ScheduleContract.Event.UpdateBookingOptions(it)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.handleEvent(ScheduleContract.Event.SubmitBooking) },
                enabled = !state.isSubmitting && state.bookerName.isNotBlank() && state.bookerPhone.length == 9
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(strings.save)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { viewModel.handleEvent(ScheduleContract.Event.DismissBooking) }) { Text(strings.cancel) }
        }
    )
}

/** Bir maydonda vaqt oralig'ini (yoki butun ish kunini) yopish. */
@Composable
private fun BlockDialog(state: ScheduleContract.State, viewModel: ScheduleViewModel, strings: Language) {
    val form = state.blockForm ?: return
    val update = { f: BlockForm -> viewModel.handleEvent(ScheduleContract.Event.UpdateBlockForm(f)) }
    AlertDialog(
        onDismissRequest = { viewModel.handleEvent(ScheduleContract.Event.DismissBlock) },
        title = { Text(strings.blockTime) },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(strings.blockTimeHint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (state.lanes.size > 1) {
                    Text(strings.pitch, style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.lanes.forEach { lane ->
                            FilterChip(
                                selected = lane.id == form.stadiumId,
                                onClick = { update(form.copy(stadiumId = lane.id)) },
                                label = { Text(lane.stadium.name.orEmpty(), maxLines = 1) }
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.wholeDay, Modifier.weight(1f))
                    Switch(checked = form.wholeDay, onCheckedChange = { update(form.copy(wholeDay = it)) })
                }
                if (!form.wholeDay) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TimeStepper(strings.timeFrom, form.from, Modifier.weight(1f), SNAP) { update(form.copy(from = it)) }
                        TimeStepper(strings.timeTo, form.to, Modifier.weight(1f), SNAP) { update(form.copy(to = it)) }
                    }
                }
                OutlinedTextField(
                    value = form.reason,
                    onValueChange = { update(form.copy(reason = it.take(255))) },
                    label = { Text("${strings.blockReason} (${strings.optional})") },
                    placeholder = { Text(strings.blockReasonPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.handleEvent(ScheduleContract.Event.SubmitBlock) },
                enabled = !state.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(strings.blockTime)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { viewModel.handleEvent(ScheduleContract.Event.DismissBlock) }) { Text(strings.cancel) }
        }
    )
}

@Composable
private fun UnblockDialog(state: ScheduleContract.State, viewModel: ScheduleViewModel, strings: Language) {
    val block = state.unblock ?: return
    AlertDialog(
        onDismissRequest = { viewModel.handleEvent(ScheduleContract.Event.DismissUnblock) },
        title = { Text(strings.reopenTime) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(
                            "${block.stadiumName.orEmpty()} · ${hm(toMinutes(block.startTime) ?: 0)} - ${hm(toMinutes(block.endTime) ?: 0)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        block.reason?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Text(strings.reopenConfirm, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.handleEvent(ScheduleContract.Event.ConfirmUnblock) }, enabled = !state.isSubmitting) {
                Text(strings.reopenTime)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { viewModel.handleEvent(ScheduleContract.Event.DismissUnblock) }) { Text(strings.cancel) }
        }
    )
}
