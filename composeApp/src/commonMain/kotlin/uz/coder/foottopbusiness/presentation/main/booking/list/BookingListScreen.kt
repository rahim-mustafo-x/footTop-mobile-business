package uz.coder.foottopbusiness.presentation.main.booking.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.Money
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.ui.shimmer
import uz.coder.foottopbusiness.core.visualTransformation.formatPhoneNumber

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import uz.coder.foottopbusiness.core.ui.Error
import uz.coder.foottopbusiness.core.ui.Success
import uz.coder.foottopbusiness.core.ui.Warning
import uz.coder.foottopbusiness.presentation.main.booking.details.BookingDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(viewModel: BookingListViewModel, onBack: (() -> Unit)?) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.currentOrThrow
    val strings = Localization.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BookingListContract.Effect.NavigateBack -> onBack?.invoke()
                is BookingListContract.Effect.ShowToast ->
                    snackbarHostState.showSnackbar(effect.message)
                is BookingListContract.Effect.NavigateToDetails -> {
                    navigator.push(BookingDetailsScreen(effect.booking))
                }
                BookingListContract.Effect.BookingConfirmed ->
                    snackbarHostState.showSnackbar(strings.bookingConfirmedMsg)
                BookingListContract.Effect.BookingRejected ->
                    snackbarHostState.showSnackbar(strings.bookingRejectedMsg)
            }
        }
    }
    val tabs = listOf(
        strings.seeAll, strings.kelgusi, strings.faol,
        strings.yakunlangan, strings.bekorQilingan, strings.pendingTab
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                TopAppBar(
                    title = { Text(strings.bookingListTitle, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        // Ildiz tab sifatida ochilganda qaytadigan joy yo'q
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                            }
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(6) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .shimmer()
                        )
                    }
                }
            } else {
                val bookings = state.filteredBookings
                
                if (bookings.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(strings.noBookingsYet, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(bookings) { index, booking ->
                            // Ro'yxat oxiriga yaqinlashganda keyingi sahifani yuklaymiz
                            if (index == bookings.lastIndex && state.canLoadMore) {
                                LaunchedEffect(bookings.size) {
                                    viewModel.handleEvent(BookingListContract.Event.LoadMore)
                                }
                            }
                            BookingItem(
                                booking = booking,
                                isProcessing = state.processingBookingId == booking.id,
                                onCancelClick = { booking.id?.let { viewModel.handleEvent(BookingListContract.Event.OpenCancelDialog(it)) } },
                                onConfirmClick = { booking.id?.let { viewModel.handleEvent(BookingListContract.Event.ConfirmBooking(it)) } },
                                onRejectClick = { booking.id?.let { viewModel.handleEvent(BookingListContract.Event.OpenRejectDialog(it)) } },
                                onClick = { viewModel.handleEvent(BookingListContract.Event.SelectBooking(booking)) }
                            )
                        }

                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(strokeWidth = 2.dp) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showCancelDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(BookingListContract.Event.DismissCancelDialog) },
            title = { Text(strings.cancelBooking) },
            text = {
                Column {
                    Text(strings.cancelBookingConfirm)
                    Spacer(Modifier.height(16.dp))
                    
                    val isError = state.cancelReason.isBlank()
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isError) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f) 
                                             else Color.Transparent
                        )
                    ) {
                        OutlinedTextField(
                            value = state.cancelReason,
                            onValueChange = { viewModel.handleEvent(BookingListContract.Event.UpdateCancelReason(it)) },
                            label = { Text(strings.cancelReason) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            isError = isError,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
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
                    Text(strings.cancelBooking)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.handleEvent(BookingListContract.Event.DismissCancelDialog) }) {
                    Text(strings.back)
                }
            }
        )
    }

    if (state.showRejectDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(BookingListContract.Event.DismissRejectDialog) },
            title = { Text(strings.rejectBooking) },
            text = {
                OutlinedTextField(
                    value = state.rejectReason,
                    onValueChange = { viewModel.handleEvent(BookingListContract.Event.UpdateRejectReason(it)) },
                    label = { Text(strings.rejectReason) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = state.rejectReason.isBlank(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        state.bookingToReject?.let {
                            viewModel.handleEvent(
                                BookingListContract.Event.SubmitReject(it, state.rejectReason)
                            )
                        }
                    },
                    enabled = state.rejectReason.isNotBlank() && state.processingBookingId == null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.rejectBooking)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.handleEvent(BookingListContract.Event.DismissRejectDialog) }) {
                    Text(strings.back)
                }
            }
        )
    }
}

@Composable
fun BookingItem(
    booking: BookingResponseDto,
    isProcessing: Boolean,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onRejectClick: () -> Unit,
    onClick: () -> Unit
) {
    val strings = Localization.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (booking.status) {
                    "CONFIRMED" -> Success
                    "PENDING" -> Warning
                    "CANCELLED" -> Error
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
                
                // Kutilayotgan bronda bekor qilish emas, tasdiqlash/rad etish kerak --
                // ular pastdagi tugmalar qatorida.
                if (booking.status != "CANCELLED" && booking.status != "REJECTED" &&
                    booking.status != "PENDING"
                ) {
                    IconButton(onClick = onCancelClick) {
                        Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(booking.name ?: strings.unknownUser, fontWeight = FontWeight.Bold)
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
                Text(strings.totalPriceLabel + ":", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    Money.withCurrency(booking.totalPrice ?: 0.0, strings.currency),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Stadion egasining asosiy amali: kelgan so'rovni tasdiqlash yoki rad etish
            if (booking.status == "PENDING") {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(strings.rejectBooking)
                    }
                    Button(
                        onClick = onConfirmClick,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(strings.confirmBooking)
                        }
                    }
                }
            }
        }
    }
}
