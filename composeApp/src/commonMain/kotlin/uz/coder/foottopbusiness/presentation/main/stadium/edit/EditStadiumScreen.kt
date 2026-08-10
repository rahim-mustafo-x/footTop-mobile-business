package uz.coder.foottopbusiness.presentation.main.stadium.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.GradientHeader
import uz.coder.foottopbusiness.core.platform.LocationPermissionLauncher
import uz.coder.foottopbusiness.core.visualTransformation.AmountTransformation
import uz.coder.foottopbusiness.core.visualTransformation.PhoneTransformation
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.StadiumType
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.TimePickerDialog
import uz.coder.foottopbusiness.presentation.main.stadium.edit.components.LocationPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStadiumScreen(viewModel: EditStadiumViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.currentOrThrow
    val hostState = remember { SnackbarHostState() }
    val strings = Localization.current

    BackHandler { onBack() }

    LocationPermissionLauncher(
        trigger = state.triggerLocationPermission,
        onResult = { status ->
            viewModel.handleEvent(EditStadiumContract.Event.OnLocationPermissionResult(status))
        }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditStadiumContract.Effect.NavigateBack -> onBack()
                is EditStadiumContract.Effect.ShowToast -> {
                    hostState.showSnackbar(ErrorMapper.map(effect.message, strings))
                }
            }
        }
    }

    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }
    var showFeatureComingSoonDialog by remember { mutableStateOf(false) }

    if (showFeatureComingSoonDialog) {
        AlertDialog(
            onDismissRequest = { showFeatureComingSoonDialog = false },
            title = { Text(strings.addPhoto) },
            text = { Text(strings.featureComingSoon) },
            confirmButton = {
                TextButton(onClick = { showFeatureComingSoonDialog = false }) {
                    Text(strings.understand)
                }
            }
        )
    }

    if (showOpenTimePicker) {
        val initialHour = try { state.openTime.split(":")[0].toInt() } catch (_: Exception) { 8 }
        val initialMinute = try { state.openTime.split(":")[1].toInt() } catch (_: Exception) { 0 }
        
        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = { showOpenTimePicker = !showOpenTimePicker },
            onConfirm = { hour, minute ->
                val t = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                viewModel.handleEvent(EditStadiumContract.Event.OpenTime(t))
                showOpenTimePicker = !showOpenTimePicker
            }
        )
    }

    if (showCloseTimePicker) {
        val initialHour = try { state.closeTime.split(":")[0].toInt() } catch (_: Exception) { 22 }
        val initialMinute = try { state.closeTime.split(":")[1].toInt() } catch (_: Exception) { 0 }
        
        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = { showCloseTimePicker = !showCloseTimePicker },
            onConfirm = { hour, minute ->
                val t = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                viewModel.handleEvent(EditStadiumContract.Event.CloseTime(t))
                showCloseTimePicker = !showCloseTimePicker
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GradientHeader(title = strings.editStadium, onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            strings.locationInfo,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RegionDropdown(state, viewModel)
                        DistrictDropdown(state, viewModel)
                    }

                    LabelAndField(
                        strings.tournamentName,
                        state.name,
                        "Stadion nomi",
                        icon = Icons.Default.Edit
                    ) {
                        viewModel.handleEvent(EditStadiumContract.Event.Name(it))
                    }

                    LabelAndField(
                        strings.preciseAddress,
                        state.preciseAddress,
                        strings.addressPlaceholder,
                        icon = Icons.Default.Map
                    ) {
                        viewModel.handleEvent(EditStadiumContract.Event.PreciseAddress(it))
                    }

                    LabelAndField(
                        strings.phoneNumber,
                        state.phone,
                        "901234567",
                        keyboardType = KeyboardType.Phone,
                        icon = Icons.Default.Phone,
                        visualTransformation = PhoneTransformation()
                    ) {
                        if (it.length <= 9) {
                            viewModel.handleEvent(EditStadiumContract.Event.Phone(it))
                        }
                    }
                }
            }

            if (state.userRole == UserRole.SUPER_ADMIN || state.userRole == UserRole.DISTRICT_ADMIN) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                strings.assignOwner,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OwnerDropdown(state, viewModel)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            strings.technicalInfo,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LabelAndField(
                        "${strings.description} (ixtiyoriy)",
                        state.description,
                        "Stadion haqida qo'shimcha ma'lumot...",
                        singleLine = false,
                        icon = Icons.Default.Description,
                        isError = false
                    ) {
                        viewModel.handleEvent(EditStadiumContract.Event.Description(it))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            LabelAndField(
                                strings.fieldCapacity,
                                state.capacity,
                                "3",
                                KeyboardType.Number,
                                icon = Icons.Default.GridView
                            ) {
                                viewModel.handleEvent(EditStadiumContract.Event.Capacity(it))
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SportTypeDropdown(state, viewModel)
                        }
                    }

                    LabelAndField(
                        strings.hourlyPrice,
                        state.pricePerHour,
                        "50 000",
                        KeyboardType.Number,
                        icon = Icons.Default.Payments,
                        visualTransformation = AmountTransformation()
                    ) {
                        viewModel.handleEvent(EditStadiumContract.Event.PricePerHour(it))
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LocationPicker(
                        latitude = state.latitude,
                        longitude = state.longitude,
                        onSelectOnMap = {
                            navigator.push(MapSelectionScreen(state.latitude, state.longitude) { lat, lng ->
                                viewModel.handleEvent(EditStadiumContract.Event.Latitude(lat))
                                viewModel.handleEvent(EditStadiumContract.Event.Longitude(lng))
                            })
                        }
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            strings.workingHoursAndImages,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            strings.workingHours,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                state.openTime.ifBlank { "08:00" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable { showOpenTimePicker = true }.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "-",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                state.closeTime.ifBlank { "22:00" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable { showCloseTimePicker = true }.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }

                    Button(
                        onClick = { showFeatureComingSoonDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.addPhoto, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.handleEvent(EditStadiumContract.Event.Save) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = 8.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.save.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun LabelAndField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    icon: ImageVector? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = icon?.let { { Icon(it, null, tint = (if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            isError = isError,
            minLines = if (singleLine) 1 else 3,
            maxLines = if (singleLine) 1 else 5,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionDropdown(
    state: EditStadiumContract.State,
    viewModel: EditStadiumViewModel
) {
    val strings = Localization.current
    Column {
        Text(strings.chooseRegion.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showRegionDropdown,
            onExpandedChange = { viewModel.handleEvent(EditStadiumContract.Event.ShowRegionDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedRegion?.name ?: strings.chooseRegion,
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showRegionDropdown) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            )
            ExposedDropdownMenu(
                expanded = state.showRegionDropdown,
                onDismissRequest = { viewModel.handleEvent(EditStadiumContract.Event.ShowRegionDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                state.regions.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(region.name) },
                        onClick = {
                            viewModel.handleEvent(EditStadiumContract.Event.SelectRegion(region))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistrictDropdown(
    state: EditStadiumContract.State,
    viewModel: EditStadiumViewModel
) {
    val strings = Localization.current
    Column {
        Text(strings.chooseDistrict.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showDistrictDropdown,
            onExpandedChange = { viewModel.handleEvent(EditStadiumContract.Event.ShowDistrictDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedDistrict?.name ?: strings.chooseDistrict,
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDistrictDropdown) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            )
            ExposedDropdownMenu(
                expanded = state.showDistrictDropdown,
                onDismissRequest = { viewModel.handleEvent(EditStadiumContract.Event.ShowDistrictDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                state.districts.forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district.name ?: "") },
                        onClick = {
                            viewModel.handleEvent(EditStadiumContract.Event.SelectDistrict(district))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerDropdown(
    state: EditStadiumContract.State,
    viewModel: EditStadiumViewModel
) {
    val strings = Localization.current
    Column {
        Text(strings.assignOwner.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showOwnerDropdown,
            onExpandedChange = { viewModel.handleEvent(EditStadiumContract.Event.ShowOwnerDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedOwner?.let { "${it.id} | ${it.fullName ?: it.username}" } ?: strings.chooseRegion, // Using chooseRegion as placeholder for Select
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showOwnerDropdown) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            )
            ExposedDropdownMenu(
                expanded = state.showOwnerDropdown,
                onDismissRequest = { viewModel.handleEvent(EditStadiumContract.Event.ShowOwnerDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                state.owners.forEach { owner ->
                    DropdownMenuItem(
                        text = { 
                            Text("${owner.id} | ${owner.fullName ?: owner.username ?: ""}") 
                        },
                        onClick = {
                            viewModel.handleEvent(EditStadiumContract.Event.SelectOwner(owner))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportTypeDropdown(
    state: EditStadiumContract.State,
    viewModel: EditStadiumViewModel
) {
    val strings = Localization.current
    Column {
        Text(strings.sportType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showTypeDropdown,
            onExpandedChange = { viewModel.handleEvent(EditStadiumContract.Event.ShowTypeDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = when(state.type) { StadiumType.FOOTBALL -> strings.football; StadiumType.TENNIS -> strings.tennis },
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showTypeDropdown) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            )
            ExposedDropdownMenu(
                expanded = state.showTypeDropdown,
                onDismissRequest = { viewModel.handleEvent(EditStadiumContract.Event.ShowTypeDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                StadiumType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = when(type) {
                                    StadiumType.FOOTBALL -> Icons.Default.SportsFootball
                                    StadiumType.TENNIS -> Icons.Default.SportsTennis
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            viewModel.handleEvent(EditStadiumContract.Event.Type(type))
                            viewModel.handleEvent(EditStadiumContract.Event.ShowTypeDropdown(false))
                        }
                    )
                }
            }
        }
    }
}
