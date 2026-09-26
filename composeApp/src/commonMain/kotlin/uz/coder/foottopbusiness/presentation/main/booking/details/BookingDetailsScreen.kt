package uz.coder.foottopbusiness.presentation.main.booking.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import uz.coder.foottopbusiness.core.Money
import uz.coder.foottopbusiness.core.formatAsDate
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Language
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.platform.makePhoneCall
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.ui.Error
import uz.coder.foottopbusiness.core.ui.Success
import uz.coder.foottopbusiness.core.ui.Warning
import uz.coder.foottopbusiness.core.visualTransformation.formatPhoneNumber
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

class BookingDetailsScreen(private val booking: BookingResponseDto) : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<BookingDetailsViewModel> { parametersOf(booking) }
        val navigator = LocalNavigator.currentOrThrow
        BookingDetailsContent(viewModel = viewModel, onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDetailsContent(viewModel: BookingDetailsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = Localization.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val booking = state.booking

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            val message = when (effect) {
                BookingDetailsContract.Effect.BookingConfirmed -> strings.bookingConfirmedMsg
                BookingDetailsContract.Effect.BookingRejected -> strings.bookingRejectedMsg
                BookingDetailsContract.Effect.PaymentStatusUpdated -> strings.paymentStatusUpdated
                BookingDetailsContract.Effect.SeriesUpdated -> strings.seriesUpdatedMsg
                is BookingDetailsContract.Effect.ShowError ->
                    effect.message.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) }
                        ?: strings.unknownNetworkError
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(strings.bookingDetails) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        },
        bottomBar = {
            // Stadion egasining asosiy amali: kutilayotgan bronni tasdiqlash yoki rad etish
            if (booking.status == "PENDING" && booking.id != null) {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.handleEvent(BookingDetailsContract.Event.OpenRejectDialog) },
                            enabled = !state.isProcessing,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(strings.rejectBooking)
                        }
                        Button(
                            onClick = { viewModel.handleEvent(BookingDetailsContract.Event.Confirm) },
                            enabled = !state.isProcessing,
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            if (state.isProcessing && !state.showRejectDialog) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(booking.status, strings)

            InfoSection(title = strings.customerInfo, icon = Icons.Default.Person) {
                DetailRow(Icons.Default.Person, strings.fullName, booking.name ?: strings.unknownUser)
                if (!booking.phone.isNullOrBlank()) {
                    DetailRow(Icons.Default.Phone, strings.phoneNumber, formatPhoneNumber(booking.phone))
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (!makePhoneCall(booking.phone)) {
                                scope.launch { snackbarHostState.showSnackbar(strings.cannotOpenDialer) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.call)
                    }
                }
            }

            InfoSection(title = strings.bookingInfo, icon = Icons.Default.Event) {
                val start = booking.startTime.toLocalDateTimeSafe()
                DetailRow(Icons.Default.CalendarToday, strings.date, start?.formatAsDate() ?: "—")
                DetailRow(
                    Icons.Default.AccessTime,
                    strings.time,
                    "${booking.startTime.formatToTime()} - ${booking.endTime.formatToTime()}"
                )
            }

            InfoSection(title = strings.paymentInfo, icon = Icons.Default.Payments) {
                DetailRow(
                    Icons.Default.Payments,
                    strings.totalPriceLabel,
                    Money.withCurrency(booking.totalPrice ?: 0.0, strings.currency)
                )
                booking.paymentMethod?.let { method ->
                    DetailRow(Icons.Default.CreditCard, strings.paymentMethod, paymentMethodLabel(method, strings))
                }
                booking.paymentTiming?.let { timing ->
                    DetailRow(Icons.Default.Schedule, strings.paymentTimingLabel, paymentTimingLabel(timing, strings))
                }
                val isPaid = booking.paymentStatus == "PAID"
                DetailRow(
                    if (isPaid) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                    strings.paymentStatusLabel,
                    if (isPaid) strings.paymentPaid else strings.paymentUnpaid
                )
                // Bekor qilingan bronni to'langan deb belgilab bo'lmaydi (backend ham rad etadi),
                // lekin avval qo'yilgan belgini olib tashlash mumkin
                val canMarkPaid = booking.status != "CANCELLED" && booking.status != "REJECTED"
                if (booking.id != null && (isPaid || canMarkPaid)) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.handleEvent(BookingDetailsContract.Event.TogglePaymentStatus) },
                        enabled = !state.isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isPaid) strings.markAsUnpaid else strings.markAsPaid)
                    }
                }
            }

            if (state.isRecurring) {
                InfoSection(title = strings.recurringSeries, icon = Icons.Default.Repeat) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (booking.status == "PENDING") {
                            Button(
                                onClick = { viewModel.handleEvent(BookingDetailsContract.Event.ConfirmSeries) },
                                enabled = !state.isProcessing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(strings.confirmSeries)
                            }
                            OutlinedButton(
                                onClick = { viewModel.handleEvent(BookingDetailsContract.Event.OpenRejectSeriesDialog) },
                                enabled = !state.isProcessing,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(strings.rejectSeries)
                            }
                        }
                        OutlinedButton(
                            onClick = { viewModel.handleEvent(BookingDetailsContract.Event.OpenCancelSeriesDialog) },
                            enabled = !state.isProcessing,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(strings.cancelSeries)
                        }
                    }
                }
            }
        }
    }

    if (state.showCancelSeriesDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(BookingDetailsContract.Event.DismissCancelSeriesDialog) },
            title = { Text(strings.cancelSeries) },
            text = {
                OutlinedTextField(
                    value = state.cancelSeriesReason,
                    onValueChange = { viewModel.handleEvent(BookingDetailsContract.Event.UpdateCancelSeriesReason(it)) },
                    label = { Text(strings.cancelReason) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.handleEvent(BookingDetailsContract.Event.SubmitCancelSeries) },
                    enabled = state.cancelSeriesReason.isNotBlank() && !state.isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.cancelSeries)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.handleEvent(BookingDetailsContract.Event.DismissCancelSeriesDialog) }) {
                    Text(strings.back)
                }
            }
        )
    }

    if (state.showRejectDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(BookingDetailsContract.Event.DismissRejectDialog) },
            title = { Text(if (state.rejectWholeSeries) strings.rejectSeries else strings.rejectBooking) },
            text = {
                OutlinedTextField(
                    value = state.rejectReason,
                    onValueChange = { viewModel.handleEvent(BookingDetailsContract.Event.UpdateRejectReason(it)) },
                    label = { Text(strings.rejectReason) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.handleEvent(BookingDetailsContract.Event.SubmitReject) },
                    enabled = state.rejectReason.isNotBlank() && !state.isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.rejectBooking)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.handleEvent(BookingDetailsContract.Event.DismissRejectDialog) }) {
                    Text(strings.back)
                }
            }
        )
    }
}

private fun paymentTimingLabel(timing: String, strings: Language): String = when (timing) {
    "PREPAID" -> strings.paymentPrepaid
    "PAY_AFTER_GAME" -> strings.paymentAfterGame
    else -> timing
}

private fun paymentMethodLabel(method: String, strings: Language): String = when (method) {
    "CASH" -> strings.paymentCash
    "CARD" -> strings.paymentCard
    else -> method
}

@Composable
private fun StatusCard(status: String?, strings: Language) {
    val (color, label) = when (status) {
        "CONFIRMED" -> Success to strings.statusConfirmed
        "PENDING" -> Warning to strings.statusPending
        "CANCELLED" -> Error to strings.statusCancelled
        "REJECTED" -> Error to strings.statusRejected
        "COMPLETED" -> MaterialTheme.colorScheme.primary to strings.statusCompleted
        else -> MaterialTheme.colorScheme.onSurfaceVariant to (status ?: strings.unknown)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = color)
            Spacer(Modifier.width(12.dp))
            Text(label, color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text("$label:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}
