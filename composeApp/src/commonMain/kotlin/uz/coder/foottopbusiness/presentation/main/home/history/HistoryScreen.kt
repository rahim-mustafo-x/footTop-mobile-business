package uz.coder.foottopbusiness.presentation.main.home.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.formatAsDate
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.presentation.main.home.HomeContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(state: HomeContract.State) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = true, onClick = {}, label = { Text("Hammasi") })
            FilterChip(selected = false, onClick = {}, label = { Text("O'yinlar") })
            FilterChip(selected = false, onClick = {}, label = { Text("Turnirlar") })
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Yaqindagi o'yinlar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            items(state.matches) { match ->
                MatchRow(match) {}
            }
        }
    }
}

@Composable
private fun MatchRow(match: MatchResponseDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Match #${match.id}", fontWeight = FontWeight.Bold)
                Text(
                    match.dateTime?.let { LocalDateTime.parse(it) }?.formatAsDate() ?: "",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    match.dateTime?.let { LocalDateTime.parse(it) }?.formatAsTime() ?: "",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Text(
                "${match.pricePerPlayer?.toInt()} so'm",
                color = Primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
