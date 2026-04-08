@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.stadium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.ui.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StadiumScreen(viewModel: StadiumViewModel, onNavigateToAddPitch: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    if (state.stadiumToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(StadiumContract.Event.DismissDelete) },
            title = { Text("O'chirishni tasdiqlang") },
            text = { Text("${state.stadiumToDelete?.name} stadionini rostdan ham o'chirmoqchimisiz?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.handleEvent(StadiumContract.Event.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("O'chirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(StadiumContract.Event.DismissDelete) }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

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
            } else if (state.hasError && state.stadiums.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Internet aloqasi yo'q", fontSize = 18.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.handleEvent(StadiumContract.Event.Refresh) }) {
                            Text("Qayta urinish")
                        }
                    }
                }
            } else {
                val horizontalScrollState = rememberScrollState()
                val listState = rememberLazyListState()

                // Pagination logic: load more when reaching the end
                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { lastIndex ->
                            if (lastIndex != null && lastIndex >= state.stadiums.size - 1 && !state.isLastPage && !state.isLoading) {
                                viewModel.handleEvent(StadiumContract.Event.LoadNextPage)
                            }
                        }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                            // Header
                            Row(
                                modifier = Modifier.background(Primary.copy(alpha = 0.1f)).padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Nomi", "Narxi", "Vaqti", "Holat", "Amallar").forEach { header ->
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

                            LazyColumn(state = listState, modifier = Modifier.fillMaxHeight()) {
                                if (state.stadiums.isEmpty()) {
                                    item {
                                        Text(
                                            "Ma'lumot topilmadi",
                                            modifier = Modifier.width(500.dp).padding(24.dp),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    itemsIndexed(state.stadiums) { index, item ->
                                        if (index > 0) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        }
                                        Row(
                                            modifier = Modifier.clickable {
                                                viewModel.handleEvent(StadiumContract.Event.StadiumClick(item))
                                            }.padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TableCell(item.name ?: "—")
                                            TableCell("${item.pricePerHour?.toInt() ?: 0} so'm")
                                            val openTimeStr = item.openTime?.takeIf { it.contains("T") }?.let { it.split("T").getOrNull(1)?.take(5) } ?: item.openTime?.take(5) ?: "—"
                                            val closeTimeStr = item.closeTime?.takeIf { it.contains("T") }?.let { it.split("T").getOrNull(1)?.take(5) } ?: item.closeTime?.take(5) ?: "—"
                                            TableCell("$openTimeStr-$closeTimeStr")
                                            TableCell(
                                                text = if (item.isActive == true) "Faol" else "Nofaol",
                                                color = if (item.isActive == true) Color(0xFF4CAF50) else Color.Gray
                                            )
                                            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                                                IconButton(onClick = { viewModel.handleEvent(StadiumContract.Event.RequestDelete(item)) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                                }
                                            }
                                        }
                                    }

                                    if (state.isLoading) {
                                        item {
                                            Box(modifier = Modifier.fillMaxWidth().width(500.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Navigation Arrows for Horizontal Scroll
                    if (horizontalScrollState.value > 0) {
                        Surface(
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp).size(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Black.copy(alpha = 0.3f),
                            contentColor = Color.White
                        ) {
                            IconButton(onClick = { scope.launch { horizontalScrollState.animateScrollTo(0) } }) {
                                Icon(Icons.Default.KeyboardArrowLeft, null)
                            }
                        }
                    }

                    if (horizontalScrollState.value < horizontalScrollState.maxValue) {
                        Surface(
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Black.copy(alpha = 0.3f),
                            contentColor = Color.White
                        ) {
                            IconButton(onClick = { scope.launch { horizontalScrollState.animateScrollTo(horizontalScrollState.maxValue) } }) {
                                Icon(Icons.Default.KeyboardArrowRight, null)
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
