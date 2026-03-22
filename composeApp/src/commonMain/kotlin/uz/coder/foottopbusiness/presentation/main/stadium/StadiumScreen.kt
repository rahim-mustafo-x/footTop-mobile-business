@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.stadium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.coder.foottopbusiness.core.ui.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StadiumScreen(viewModel: StadiumViewModel, onNavigateToAddPitch: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPitch,
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Stadium")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Search & Filters
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.handleEvent(StadiumContract.Event.Search(it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Qidiruv...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                )
                IconButton(onClick = { viewModel.handleEvent(StadiumContract.Event.Refresh) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
                }
            }

            if (state.isLoading && state.stadiums.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                val scrollState = rememberScrollState()
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.horizontalScroll(scrollState)) {
                        // Header
                        Row(
                            modifier = Modifier.background(Primary.copy(alpha = 0.1f)).padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Nomi", "Narxi", "Vaqti", "Holat").forEach { header ->
                                Text(
                                    text = header,
                                    modifier = Modifier.width(100.dp).padding(horizontal = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Rows
                        if (state.stadiums.isEmpty()) {
                            Text(
                                "Ma'lumot topilmadi",
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            state.stadiums.forEach { item ->
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier.clickable {
                                        viewModel.handleEvent(StadiumContract.Event.StadiumClick(item))
                                    }.padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell(item.name ?: "—")
                                    TableCell("${item.pricePerHour?.toInt() ?: 0} so'm")
                                    TableCell("${item.openTime?.take(5) ?: "—"}-${item.closeTime?.take(5) ?: "—"}")
                                    TableCell(
                                        text = if (item.isActive == true) "Faol" else "Nofaol",
                                        color = if (item.isActive == true) Color(0xFF4CAF50) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text = text,
        modifier = Modifier.width(100.dp).padding(horizontal = 8.dp),
        fontSize = 12.sp,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
