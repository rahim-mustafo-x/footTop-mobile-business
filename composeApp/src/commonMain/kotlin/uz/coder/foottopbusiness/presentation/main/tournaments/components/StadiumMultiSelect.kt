package uz.coder.foottopbusiness.presentation.main.tournaments.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsContract
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsViewModel

/**
 * Turnir o'tadigan stadionlarni tanlash.
 *
 * Turnir bir nechta stadionda o'tishi mumkin, shuning uchun ro'yxat ko'p tanlovli.
 * BIRINCHI stadion asosiy: koordinata, viloyat va tuman o'shandan olinadi.
 * Boshqasini asosiy qilish uchun tanlanganlar orasidan ustiga bosiladi.
 */
@Composable
fun StadiumMultiSelect(
    state: TournamentsContract.State,
    viewModel: TournamentsViewModel,
    isError: Boolean = false
) {
    val strings = Localization.current
    var query by remember { mutableStateOf("") }
    val expanded = state.showStadiumDropdown

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            strings.tournamentStadiums,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        state.selectedStadiums.forEachIndexed { index, stadium ->
            val isPrimary = index == 0
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable(enabled = !isPrimary) {
                            viewModel.handleEvent(TournamentsContract.Event.SetPrimaryStadium(stadium))
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isPrimary) Icons.Default.Star else Icons.Outlined.Stadium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stadium.name.orEmpty(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPrimary) strings.primaryStadium else strings.setAsPrimaryStadium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                viewModel.handleEvent(TournamentsContract.Event.ToggleStadium(stadium))
                            }
                    )
                }
            }
        }

        // Bir nechta stadion tanlansa ham serverga hozircha bittasi ketadi -
        // buni yashirmasdan aytamiz, aks holda ma'lumot jimgina yo'qoladi
        if (state.selectedStadiums.size > 1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    strings.onlyPrimaryStadiumSaved,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .clickable {
                            viewModel.handleEvent(TournamentsContract.Event.ShowStadiumDropdown(!expanded))
                        }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        strings.selectStadiumForBooking,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (expanded) "▲" else "▼",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (expanded) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(strings.search, fontSize = 13.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    val visible = state.stadiums.filter {
                        query.isBlank() || it.name.orEmpty().contains(query, ignoreCase = true)
                    }

                    // Ro'yxat uzun bo'lishi mumkin, lekin ekranning o'zi ham
                    // aylanadi - shuning uchun balandligi cheklanadi
                    Column(
                        modifier = Modifier
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (visible.isEmpty()) {
                            Text(
                                strings.notFound,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        visible.forEach { stadium ->
                            val checked = state.selectedStadiums.any { it.id == stadium.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.handleEvent(TournamentsContract.Event.ToggleStadium(stadium))
                                    }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = checked, onCheckedChange = {
                                    viewModel.handleEvent(TournamentsContract.Event.ToggleStadium(stadium))
                                })
                                Column(Modifier.weight(1f).padding(vertical = 4.dp)) {
                                    Text(
                                        stadium.name.orEmpty(),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val place = listOfNotNull(
                                        stadium.regionName?.takeIf { it.isNotBlank() },
                                        stadium.districtName?.takeIf { it.isNotBlank() }
                                    ).joinToString(", ")
                                    if (place.isNotBlank()) {
                                        Text(
                                            place,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isError) {
            Text(
                strings.selectAtLeastOneStadium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Asosiy stadiondan kelib chiqqan viloyat/tuman - faqat ko'rsatish uchun.
 *
 * Ilgari bular qo'lda tanlanadigan dropdown edi va stadion boshqa tumanda bo'lsa
 * nomuvofiqlik yuzaga kelardi.
 */
@Composable
fun DerivedLocationSummary(state: TournamentsContract.State) {
    val strings = Localization.current
    val primary = state.primaryStadium ?: return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SummaryRow(strings.chooseRegion, state.selectedRegion?.name ?: primary.regionName ?: "-")
            SummaryRow(strings.chooseDistrict, state.selectedDistrict?.name ?: primary.districtName ?: "-")
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
