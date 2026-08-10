@file:Suppress("DEPRECATION")

package uz.coder.foottopbusiness.presentation.main.stadium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.ui.HeaderIconButton
import uz.coder.foottopbusiness.core.ui.shimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StadiumScreen(viewModel: StadiumViewModel, onNavigateToAddStadium: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = Localization.current

    if (state.stadiumToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(StadiumContract.Event.DismissDelete) },
            title = { Text(strings.confirmDelete) },
            text = { Text("${state.stadiumToDelete?.name} ${strings.deleteConfirmMsg}") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.handleEvent(StadiumContract.Event.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(StadiumContract.Event.DismissDelete) }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GradientHeader(
                title = if (state.isOwner) strings.schedule else strings.stadiums,
                subtitle = if (state.isOwner) null else strings.stadiumCount(state.stadiums.size),
                actions = {
                    if (!state.isOwner) {
                        HeaderIconButton(
                            icon = Icons.Default.Add,
                            onClick = onNavigateToAddStadium,
                            contentDescription = strings.addStadium
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Modern Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 0.dp
            ) {
                TextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.handleEvent(StadiumContract.Event.Search(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(strings.search, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            if (state.isLoading && state.stadiums.isEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(6) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .shimmer()
                        )
                    }
                }
            } else {
                val listState = rememberLazyListState()

                // Pagination
                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { lastIndex ->
                            if (lastIndex != null && (lastIndex >= state.stadiums.size - 1) && !state.isLastPage && !state.isLoading) {
                                viewModel.handleEvent(StadiumContract.Event.LoadNextPage)
                            }
                        }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(state.stadiums) { _, item ->
                        StadiumListItem(
                            stadium = item,
                            onClick = { viewModel.handleEvent(StadiumContract.Event.StadiumClick(item)) }
                        )
                    }

                    if (state.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StadiumListItem(
    stadium: uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse,
    onClick: () -> Unit
) {
    val strings = Localization.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when {
                stadium.isActive == true -> Color(0xFF4CAF50)
                stadium.description?.contains("ta'mir", ignoreCase = true) == true -> Color(0xFFFF9800)
                else -> MaterialTheme.colorScheme.error
            }

            // Sport Icon with rounded background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.SportsSoccer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stadium.name ?: strings.unknown,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    // Status dot
                    Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(statusColor))
                }
                
                Text(
                    text = "${stadium.districtName}, ${stadium.regionName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StadiumBadge(Icons.Outlined.GridView, (stadium.capacity ?: 0).toString())
                    StadiumBadge(Icons.Outlined.Payments, "${stadium.pricePerHour?.toInt() ?: 0} ${strings.uzsPerHour}")
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StadiumBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
