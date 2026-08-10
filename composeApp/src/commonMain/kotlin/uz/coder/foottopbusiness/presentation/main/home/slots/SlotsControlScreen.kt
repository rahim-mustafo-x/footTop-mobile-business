package uz.coder.foottopbusiness.presentation.main.home.slots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.plusMinutes
import uz.coder.foottopbusiness.core.visualTransformation.PhoneTransformation
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

// ─── Utilities ──────────────────────────────────────────────────────────────

fun durationMinutes(key: String): Int = when(key) {
    "SIXTY" -> 60
    "NINETY" -> 90
    "ONE_HUNDRED_TWENTY" -> 120
    else -> 60
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotsControlScreen(stadium: StadiumResponse, state: HomeContract.State, viewModel: HomeViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val strings = Localization.current
    val snackbarHostState = remember { SnackbarHostState() }

    val next5Days = remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        (0..4).map { now.plus(it, DateTimeUnit.DAY).toString() }
    }

    // Yagona collector. Ilgari SlotsControlVoyager faqat NavigateBack'ni ushlab,
    // ShowToast'ni Channel'dan olib tashlardi - band qilish xatosi jimgina
    // yo'qolib ketardi.
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeContract.Effect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = ErrorMapper.map(effect.message, strings),
                        withDismissAction = true
                    )
                }
                HomeContract.Effect.NavigateBack -> navigator.pop()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stadium.name ?: strings.stadium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.handleEvent(HomeContract.Event.ClearStadiumForSlots)
                        navigator.pop()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Duration Selection
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Kenglik teng bo'lingan: uchta 100dp tugma + bo'shliqlar tor
                // ekranlarga sig'masdi
                DurationChip("60 min", state.selectedDuration == "SIXTY", Modifier.weight(1f)) { viewModel.handleEvent(HomeContract.Event.ChangeDuration("SIXTY")) }
                DurationChip("90 min", state.selectedDuration == "NINETY", Modifier.weight(1f)) { viewModel.handleEvent(HomeContract.Event.ChangeDuration("NINETY")) }
                DurationChip("120 min", state.selectedDuration == "ONE_HUNDRED_TWENTY", Modifier.weight(1f)) { viewModel.handleEvent(HomeContract.Event.ChangeDuration("ONE_HUNDRED_TWENTY")) }
            }

            // Days Selection
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(next5Days) { dateStr ->
                    val isSelected = state.selectedDate == dateStr
                    val dateParts = dateStr.split("-")
                    val day = dateParts.last()
                    
                    Column(
                        modifier = Modifier
                            // "Сегодня" kabi uzunroq yozuvlar uchun 64dp tor edi
                            .widthIn(min = 64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.handleEvent(HomeContract.Event.ChangeDate(dateStr)) }
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        val label = when(dateStr) {
                            next5Days[0] -> strings.today
                            next5Days[1] -> strings.tomorrow
                            else -> ""
                        }
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(strings.freeSlots, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                
            state.stadiumSlots.firstOrNull { it.third }?.let { (start, _, _) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("${strings.nearestSlot}: ", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(start.formatAsTime(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            if (state.isLoadingSlots) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) 
                }
            } else if (state.stadiumSlots.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.EventNote, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                        Text(strings.noSlotsToday, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val durationMins = durationMinutes(state.selectedDuration)
                val selectedStart = state.selectedSlot?.first
                val selectedEnd = selectedStart?.plusMinutes(durationMins)

                // Chekka va oraliqlar biroz toraytirildi - shu hisobga katakcha
                // ichida matn uchun joy ko'proq qoladi
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.stadiumSlots.size) { index ->
                        val slot = state.stadiumSlots[index]
                        val (start, _, available) = slot

                        // Katakcha faqat o'z boshlanish vaqti tanlangan oraliq
                        // ichida bo'lsa belgilanadi. Ilgari bu yerda slot'ning
                        // API'dan kelgan tugash vaqti bilan kesishish tekshirilardi
                        // - slot oxiri esa tanlangan davomiylikka teng (12:30 ->
                        // 13:30), shuning uchun 13:00 ni tanlaganda oldingi 12:30
                        // katakchasi ham yonib turardi.
                        val isPartiallySelected = selectedStart != null && selectedEnd != null &&
                                                 start >= selectedStart && start < selectedEnd
                        
                        // Bo'sh emas slotlarning hammasi ham band emas: 16:00-17:00
                        // broni 15:30 ni ham yopadi, chunki u yerdan 1 soatlik
                        // o'yin boshlab bo'lmaydi. Bunday slot qizil emas, kulrang
                        // ko'rsatiladi - vaqti bo'sh, faqat davomiylik sig'maydi.
                        val isOccupied = !available && start in state.occupiedSlotStarts

                        val primaryColor = MaterialTheme.colorScheme.primary
                        val statusColor = when {
                            available -> primaryColor
                            isOccupied -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }

                        val baseColor = if (isPartiallySelected) MaterialTheme.colorScheme.onPrimary else statusColor
                        val bgColor = if (isPartiallySelected) primaryColor else statusColor.copy(alpha = 0.1f)
                        val borderColor = if (isPartiallySelected) primaryColor else statusColor.copy(alpha = 0.3f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                                .clickable(enabled = available) {
                                    viewModel.handleEvent(HomeContract.Event.SelectSlot(slot))
                                }
                                // Ilgari yon bo'shliq umuman yo'q edi - matn
                                // katakcha chetiga tegib, uzunroq tarjimalarda
                                // ("Свободно", "Забронировано") qirqilib qolardi
                                .padding(horizontal = 6.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPartiallySelected) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = baseColor.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(12.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    start.formatAsTime(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = baseColor,
                                    maxLines = 1
                                )
                                // Uzun tarjima sig'masa qirqilmasin, ikkinchi
                                // qatorga o'tsin
                                Text(
                                    when {
                                        available -> strings.free
                                        isOccupied -> strings.booked
                                        else -> strings.slotNotEnough
                                    },
                                    fontSize = 11.sp,
                                    color = baseColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(strings.free, MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    LegendItem(strings.booked, MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(16.dp))
                    // Bu ekranda "o'tgan" holati alohida chizilmaydi - kulrang
                    // katakcha davomiylik sig'maydigan slotni bildiradi
                    LegendItem(strings.slotNotEnough, MaterialTheme.colorScheme.outline)
                }
            }
        }

        if (state.isBookingSlot && state.selectedSlot != null) {
            val durationMins = durationMinutes(state.selectedDuration)
            val selectedStart = state.selectedSlot.first
            val selectedEnd = selectedStart.plusMinutes(durationMins)
            
            var fullName by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            val durationText = when(state.selectedDuration) {
                "SIXTY" -> "60 min"
                "NINETY" -> "90 min"
                "ONE_HUNDRED_TWENTY" -> "120 min"
                else -> ""
            }

            val startTimeStr = selectedStart.formatAsTime()
            val endTimeStr = selectedEnd.formatAsTime()

            AlertDialog(
                onDismissRequest = { viewModel.handleEvent(HomeContract.Event.DismissBookingDialog) },
                // Sarlavhada faqat amal nomi. Ilgari bu yerda vaqt oralig'i ham
                // turardi ("Забронировать: 14:00 - 15:00") - rus tilida bu qator
                // sarlavha o'lchamida ikki-uch qatorga cho'zilib, dialog ichidagi
                // maydonlarni ekrandan chiqarib yuborardi.
                title = { Text(strings.bookNow) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Vaqt va davomiylik - alohida blokda, uzun tarjimada ham
                        // o'zining joyi bor
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "$startTimeStr - $endTimeStr",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${strings.duration}: $durationText",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(strings.fullName) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )

                        // `phone` faqat 9 ta raqamni saqlaydi ("901234567") -
                        // backend shu ko'rinishni kutadi. Qavs-tirelar esa faqat
                        // ko'rinishda: klaviatura Number bo'lsa ham nusxa-joylashtirish
                        // orqali harf tushishi mumkin, shuning uchun filtr kerak.
                        val phoneIncomplete = phone.isNotEmpty() && phone.length < 9
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { input -> phone = input.filter { it.isDigit() }.take(9) },
                            label = { Text(strings.phoneNumber) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            prefix = { Text("+998 ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PhoneTransformation(),
                            isError = phoneIncomplete,
                            supportingText = if (phoneIncomplete) {
                                { Text(strings.enterFullPhone) }
                            } else null,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    null,
                                    tint = if (phoneIncomplete) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.handleEvent(HomeContract.Event.CreateBooking(fullName, phone)) },
                        enabled = fullName.isNotBlank() && phone.length == 9
                    ) {
                        Text(strings.save)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.handleEvent(HomeContract.Event.DismissBookingDialog) }) {
                        Text(strings.cancel)
                    }
                }
            )
        }
    }
}

@Composable
fun DurationChip(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
