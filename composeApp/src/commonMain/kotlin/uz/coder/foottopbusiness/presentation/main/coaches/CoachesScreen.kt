package uz.coder.foottopbusiness.presentation.main.coaches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachesScreen(viewModel: CoachesViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CoachesContract.Effect.ShowToast -> { /* snackbar */ }
            }
        }
    }

    if (state.selectedCoach != null) {
        CoachDetailScreen(
            coach = state.selectedCoach!!,
            onBack = { viewModel.handleEvent(CoachesContract.Event.ClearDetail) }
        )
        return
    }

    if (state.showCreateDialog) {
        CreateCoachDialog(
            onDismiss = { viewModel.handleEvent(CoachesContract.Event.HideCreateDialog) },
            onCreate = { userId, specialty, expYears, rate, availability ->
                viewModel.handleEvent(CoachesContract.Event.Create(userId, specialty, expYears, rate, availability))
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = statusBarPadding, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Foydalanuvchilar",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { viewModel.handleEvent(CoachesContract.Event.ShowCreateDialog) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), start = paddingValues.calculateStartPadding(
                    LayoutDirection.Ltr), end = paddingValues.calculateEndPadding(LayoutDirection.Rtl))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Search and Filters
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.handleEvent(CoachesContract.Event.Search(it)) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Qidirish...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                    Text("Filter", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }

                Spacer(Modifier.height(16.dp))

                val filterTabs = listOf("Barchasi", "Admin", "Egasi", "Coach")

                ScrollableTabRow(
                    selectedTabIndex = state.selectedRoleFilter,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    filterTabs.forEachIndexed { index, title ->
                            Tab(
                                selected = state.selectedRoleFilter == index,
                                onClick = { viewModel.handleEvent(CoachesContract.Event.FilterByRole(index)) },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (state.selectedRoleFilter == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    title,
                                    color = if (state.selectedRoleFilter == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
            }

            when {
                state.isLoading || state.isCreating -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { viewModel.handleEvent(CoachesContract.Event.Load) }) { Text("Qayta urinish") }
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredCoaches, key = { it.id ?: 0L }) { coach ->
                        UserListItem(coach = coach, onClick = { viewModel.handleEvent(CoachesContract.Event.SelectCoach(coach)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(coach: CoachResponseDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initial = coach.coachName?.firstOrNull()?.toString() ?: "U"
                Text(initial, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(coach.coachName ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(coach.specialty ?: "user@malaeb.uz", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Role and Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Coach", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCoachDialog(
    onDismiss: () -> Unit,
    onCreate: (userId: Long, specialty: String, experienceYears: Int, hourlyRate: Double, availability: String?) -> Unit,
) {
    var userId by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("MURABBIY") }
    var expYears by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }

    val specialties = listOf("MURABBIY", "ADMIN", "EGASI")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi murabbiy", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = userId, onValueChange = { userId = it.filter { c -> c.isDigit() } },
                    label = { Text("Foydalanuvchi ID") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mutaxassislik") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        specialties.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    specialty = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = expYears, onValueChange = { expYears = it.filter { c -> c.isDigit() } },
                    label = { Text("Tajriba (yil)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = hourlyRate, onValueChange = { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Soatlik narx (so'm)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = availability, onValueChange = { availability = it },
                    label = { Text("Mavjudlik (ixtiyoriy)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userId.isNotBlank() && specialty.isNotBlank()) {
                        onCreate(userId.toLongOrNull() ?: 0L, specialty,
                            expYears.toIntOrNull() ?: 0, hourlyRate.toDoubleOrNull() ?: 0.0,
                            availability.takeIf { it.isNotBlank() })
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Qo'shish") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Bekor qilish", color = MaterialTheme.colorScheme.secondary) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoachDetailScreen(coach: CoachResponseDto, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(coach.coachName ?: "Murabbiy") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(coach.coachName ?: "—", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(coach.specialty ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            DetailRow("Tajriba", "${coach.experienceYears ?: 0} yil")
            DetailRow("Narx", "${coach.hourlyRate?.toInt() ?: 0} so'm/soat")
            DetailRow("Mavjudlik", coach.availability ?: "—")
            if (!coach.reviews.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Sharhlar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(coach.reviews, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
    HorizontalDivider()
}
