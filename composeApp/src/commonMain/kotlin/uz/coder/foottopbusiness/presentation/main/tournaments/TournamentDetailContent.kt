package uz.coder.foottopbusiness.presentation.main.tournaments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.coder.foottopbusiness.core.Money
import uz.coder.foottopbusiness.core.formatToTime
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto

/**
 * Turnir tafsilotlari - Bosh sahifa va Turnirlar bo'limi uchun umumiy ekran.
 * Ilgari ikki joyda alohida nusxa bor edi, Turnirlar'dagisi esa bo'sh qolgan edi.
 *
 * @param onEdit null bo'lsa tahrirlash tugmasi ko'rsatilmaydi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailContent(
    tournament: TournamentResponseDto,
    onBack: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val strings = Localization.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.tournamentDetails, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = strings.edit,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            tournament.name ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            if (tournament.sportType == null || tournament.sportType == "FOOTBALL") strings.football
                            else tournament.sportType,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                TournamentInfoSection(strings.location, tournament.address ?: strings.noDataYet, Icons.Default.LocationOn)
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TournamentInfoSection(
                        strings.tournamentDate,
                        rangeOrNoData(tournament.startDate, tournament.endDate, strings.noDataYet),
                        Icons.Default.CalendarToday,
                        Modifier.weight(1f)
                    )
                    TournamentInfoSection(
                        strings.tournamentTime,
                        rangeOrNoData(
                            tournament.startTime?.formatToTime(),
                            tournament.endTime?.formatToTime(),
                            strings.noDataYet
                        ),
                        Icons.Default.AccessTime,
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TournamentInfoSection(
                        strings.participants,
                        "${tournament.teamApplied ?: 0} / ${tournament.maxTeams ?: 0}",
                        Icons.Default.Groups,
                        Modifier.weight(1f)
                    )
                    TournamentInfoSection(
                        strings.entryFee,
                        Money.withCurrency(tournament.entryFee ?: 0.0, strings.currency),
                        Icons.Default.Payments,
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                TournamentInfoSection(strings.prizes, tournament.prizes ?: strings.noDataYet, Icons.Default.EmojiEvents)
            }

            item {
                TournamentInfoSection(strings.rules, tournament.rules ?: strings.noDataYet, Icons.Default.Description)
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

/** "a - b"; ikkalasi ham bo'sh bo'lsa "ma'lumot yo'q" (ilgari "null - null" chiqardi). */
private fun rangeOrNoData(start: String?, end: String?, noData: String): String {
    val s = start?.takeIf { it.isNotBlank() }
    val e = end?.takeIf { it.isNotBlank() }
    return when {
        s != null && e != null -> "$s - $e"
        s != null -> s
        e != null -> e
        else -> noData
    }
}

@Composable
private fun TournamentInfoSection(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(value, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
