package uz.coder.foottopbusiness.presentation.main.booking.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.visualTransformation.formatPhoneNumber

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import uz.coder.foottopbusiness.presentation.main.booking.details.BookingDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(viewModel: BookingListViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.currentOrThrow
    val strings = Localization.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BookingListContract.Effect.NavigateBack -> onBack()
                is BookingListContract.Effect.ShowToast -> { /* Show toast */ }
                is BookingListContract.Effect.NavigateToDetails -> {
                    navigator.push(BookingDetailsScreen(effect.booking))
                }
            }
        }
    }
    val tabs = listOf("Barchasi", "Kelgusi", "Faol", "Yakunlangan", "Bekor qilingan")

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                TopAppBar(
                    title = { Text("Bronlar ro'yxati", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTab == index,
                            onClick = { viewModel.handleEvent(BookingListContract.Event.ChangeTab(index)) },
                            text = { Text(title, fontSize = 13.sp) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.handleEvent(BookingListContract.Event.Refresh) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (state.isLoading && !state.isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val bookings = state.filteredBookings
                
                if (bookings.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hozircha bronlar yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings) { booking ->
                            BookingItem(
                                booking = booking,
                                onCancelClick = { booking.id?.let { viewModel.handleEvent(BookingListContract.Event.OpenCancelDialog(it)) } },
                                onClick = { viewModel.handleEvent(BookingListContract.Event.SelectBooking(booking)) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showCancelDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(BookingListContract.Event.DismissCancelDialog) },
            title = { Text("Bronni bekor qilish") },
            text = {
                Column {
                    Text("Haqiqatan ham ushbu bronni bekor qilmoqchimisiz?")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.cancelReason,
                        onValueChange = { viewModel.handleEvent(BookingListContract.Event.UpdateCancelReason(it)) },
                        label = { Text("Bekor qilish sababi (shart)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        state.bookingToCancel?.let {
                            viewModel.handleEvent(BookingListContract.Event.ConfirmCancelBooking(it, state.cancelReason))
                        }
                    },
                    enabled = state.cancelReason.isNotBlank()
                ) {
                    Text("Bekor qilish")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.handleEvent(BookingListContract.Event.DismissCancelDialog) }) {
                    Text("Orqaga")
                }
            }
        )
    }
}

@Composable
fun BookingItem(booking: BookingResponseDto, onCancelClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (booking.status) {
                    "CONFIRMED" -> Color(0xFF4CAF50)
                    "PENDING" -> Color(0xFFFFA000)
                    "CANCELLED" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = booking.status ?: "",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                
                if (booking.status != "CANCELLED") {
                    IconButton(onClick = onCancelClick) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Red)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(booking.name ?: "Noma'lum foydalanuvchi", fontWeight = FontWeight.Bold)
            }
            
            if (!booking.phone.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(formatPhoneNumber(booking.phone))
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                val start = booking.startTime.toLocalDateTimeSafe()
                Text("${start?.date} | ${booking.startTime.formatToTime()} - ${booking.endTime.formatToTime()}")
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Umumiy narx:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${booking.totalPrice?.toInt() ?: 0} so'm", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
