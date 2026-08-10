package uz.coder.foottopbusiness.presentation.main.coaches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.ui.HeaderIconButton
import uz.coder.foottopbusiness.presentation.main.components.UserCardItem
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto
import uz.coder.foottopbusiness.presentation.main.coaches.create.CoachCreateScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachesScreen(viewModel: CoachesViewModel) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val strings = Localization.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CoachesContract.Effect.ShowToast -> { 
                    // Use a snackbar here if you add one to Scaffold
                }
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GradientHeader(
                title = strings.users,
                actions = {
                    HeaderIconButton(
                        icon = Icons.Default.Add,
                        onClick = { navigator.push(uz.coder.foottopbusiness.presentation.main.home.user.UserCreateTypeScreen()) },
                        contentDescription = strings.createUser
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), start = paddingValues.calculateStartPadding(
                    LayoutDirection.Ltr), end = paddingValues.calculateEndPadding(LayoutDirection.Rtl))
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
                        placeholder = { Text("${strings.search}...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                    Text(strings.filter, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }

                Spacer(Modifier.height(16.dp))

                val filterTabs = listOf(strings.active, strings.addDistrictAdmin.substringAfter(" "), strings.assignOwner.substringAfter(" "), "Coach") // Roughly mapping

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
                        TextButton(onClick = { viewModel.handleEvent(CoachesContract.Event.Load) }) { Text(strings.refresh) }
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.filteredCoaches, key = { it.id ?: 0L }) { coach ->
                        UserCardItem(
                            name = coach.coachName ?: "—",
                            role = "Coach",
                            subtitle = coach.specialty,
                            status = coach.availability ?: "Aktiv",
                            onClick = { viewModel.handleEvent(CoachesContract.Event.SelectCoach(coach)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoachDetailScreen(coach: CoachResponseDto, onBack: () -> Unit) {
    val strings = Localization.current
    BackHandler(enabled = true) {
        onBack()
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GradientHeader(title = strings.coachInfo, onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(
                            coach.coachName ?: "—",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            coach.specialty ?: strings.addCoach,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Stats Section
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRow(
                        label = strings.experience,
                        value = "${coach.experienceYears ?: 0} ${strings.experienceYears}",
                        icon = Icons.Default.Star
                    )
                    DetailRow(
                        label = strings.hourlyPrice,
                        value = "${coach.hourlyRate?.toInt() ?: 0} ${strings.uzsPerHour}",
                        icon = Icons.Default.ShoppingCart
                    )
                    DetailRow(
                        label = strings.availability,
                        value = coach.availability ?: strings.unknown,
                        icon = Icons.Default.Info
                    )
                }
            }

            // Reviews Section
            if (!coach.reviews.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                strings.reviews,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            coach.reviews,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
