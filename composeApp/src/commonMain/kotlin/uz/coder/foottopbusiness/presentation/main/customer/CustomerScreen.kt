package uz.coder.foottopbusiness.presentation.main.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.core.parameter.parametersOf
import uz.coder.foottopbusiness.core.Money
import uz.coder.foottopbusiness.core.formatAsDate
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.platform.makePhoneCall
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.visualTransformation.formatPhoneNumber
import uz.coder.foottopbusiness.presentation.main.booking.details.BookingDetailsScreen
import uz.coder.foottopbusiness.presentation.main.components.BookingRowItem

/** "Kelmadi" shu sondan boshlab mijoz haqida ogohlantiramiz. */
private const val UNRELIABLE_NO_SHOWS = 2
/** Shuncha o'ynagan mijoz - doimiy mijoz. */
private const val REGULAR_GAMES = 5

/** Bitta telefon raqam bo'yicha egasi biladigan hamma narsa. */
class CustomerScreen(private val phone: String, private val customerName: String?) : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<CustomerViewModel> { parametersOf(phone) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow
        val strings = Localization.current
        val s = state.summary
        val name = s?.name ?: customerName

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.customerHistory) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                        }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (name ?: "?").take(1).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            name ?: strings.unknownUser,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(formatPhoneNumber(phone), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val badge = when {
                            s == null || s.noShowCount >= UNRELIABLE_NO_SHOWS -> null
                            s.playedCount >= REGULAR_GAMES -> strings.regularCustomer
                            s.totalBookings <= 1 -> strings.newCustomer
                            else -> null
                        }
                        if (badge != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    badge,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(onClick = { makePhoneCall(phone) }) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.call)
                        }
                    }
                }

                when {
                    state.isLoading && s == null -> Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                    state.error != null && s == null -> Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.error?.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) }
                                ?: strings.loadFailed,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { viewModel.handleEvent(CustomerContract.Event.Load) }) {
                            Text(strings.retry)
                        }
                    }

                    s != null -> {
                        if (s.noShowCount >= UNRELIABLE_NO_SHOWS) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.unreliableCustomer, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatBox(strings.statPlayed, s.playedCount, Modifier.weight(1f))
                            StatBox(strings.statUpcoming, s.upcomingCount, Modifier.weight(1f))
                            StatBox(strings.statCancelled, s.cancelledCount, Modifier.weight(1f))
                            StatBox(
                                strings.statNoShow,
                                s.noShowCount,
                                Modifier.weight(1f),
                                if (s.noShowCount > 0) MaterialTheme.colorScheme.error else null
                            )
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                KeyValue(strings.paidTotal, Money.withCurrency(s.paidAmount, strings.currency))
                                KeyValue(
                                    strings.debt,
                                    Money.withCurrency(s.unpaidAmount, strings.currency),
                                    if (s.unpaidAmount > 0) MaterialTheme.colorScheme.error else null
                                )
                                s.firstBookingAt.toLocalDateTimeSafe()?.let {
                                    KeyValue(strings.firstBooking, it.formatAsDate())
                                }
                            }
                        }

                        Text(
                            strings.recentBookings,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val recent = s.recent.orEmpty()
                        if (recent.isEmpty()) {
                            Text(strings.noBookingsYet, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            recent.forEach { b ->
                                BookingRowItem(booking = b, onClick = { navigator.push(BookingDetailsScreen(b)) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: Int, modifier: Modifier, color: Color? = null) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color ?: MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun KeyValue(label: String, value: String, color: Color? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, color = color ?: MaterialTheme.colorScheme.onSurface)
    }
}
