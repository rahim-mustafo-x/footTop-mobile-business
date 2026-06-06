package uz.coder.foottopbusiness.presentation.main.stadium.addstadium

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.core.platform.LocationPermissionLauncher
import uz.coder.foottopbusiness.core.visualTransformation.AmountTransformation
import uz.coder.foottopbusiness.core.visualTransformation.PhoneTransformation
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.stadium.edit.MapSelectionScreen
import uz.coder.foottopbusiness.presentation.main.stadium.edit.components.LocationPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStadiumScreen(viewModel: AddStadiumViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val hostState = remember { SnackbarHostState() }
    val strings = Localization.current

    BackHandler { onBack() }

    LocationPermissionLauncher(
        trigger = state.triggerLocationPermission,
        onResult = { status ->
            viewModel.handleEvent(AddStadiumContract.Event.OnLocationPermissionResult(status))
        }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddStadiumContract.Effect.NavigateBack -> onBack()
                is AddStadiumContract.Effect.ShowToast -> {
                    val mappedMessage = ErrorMapper.map(effect.message, strings)
                    log("AddStadium", "Effect message: $mappedMessage")
                    hostState.showSnackbar(mappedMessage)
                }
            }
        }
    }

    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }

    if (showOpenTimePicker) {
        val initialHour = try { state.openTime.split(":")[0].toInt() } catch (_: Exception) { 8 }
        val initialMinute = try { state.openTime.split(":")[1].toInt() } catch (_: Exception) { 0 }
        
        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = { showOpenTimePicker = !showOpenTimePicker },
            onConfirm = { hour, minute ->
                val t = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                viewModel.handleEvent(AddStadiumContract.Event.OpenTime(t))
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
                viewModel.handleEvent(AddStadiumContract.Event.CloseTime(t))
                showCloseTimePicker = !showCloseTimePicker
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(top = statusBarPadding, start = 8.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        strings.newStadium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
            val locationError = state.showErrors && (state.selectedRegion == null || state.selectedDistrict == null || state.name.isBlank())
            
            PremiumCard(
                title = strings.locationInfo,
                icon = Icons.Outlined.LocationOn,
                isError = locationError
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RegionDropdown(state, viewModel, isError = state.showErrors && state.selectedRegion == null)
                    DistrictDropdown(state, viewModel, isError = state.showErrors && state.selectedDistrict == null)
                }

                LabelAndField(
                    strings.tournamentName,
                    state.name,
                    "Stadion nomi",
                    icon = Icons.Outlined.Edit,
                    isError = state.showErrors && state.name.isBlank()
                ) {
                    viewModel.handleEvent(AddStadiumContract.Event.Name(it))
                }

                LabelAndField(
                    strings.preciseAddress,
                    state.preciseAddress,
                    strings.addressPlaceholder,
                    icon = Icons.Outlined.Map
                ) {
                    viewModel.handleEvent(AddStadiumContract.Event.PreciseAddress(it))
                }

                LabelAndField(
                    strings.phoneNumber,
                    state.phone,
                    "901234567",
                    keyboardType = KeyboardType.Phone,
                    icon = Icons.Outlined.Phone,
                    visualTransformation = PhoneTransformation()
                ) {
                    if (it.length <= 9) {
                        viewModel.handleEvent(AddStadiumContract.Event.Phone(it))
                    }
                }
            }

            if (state.userRole == UserRole.SUPER_ADMIN || state.userRole == UserRole.DISTRICT_ADMIN) {
                PremiumCard(
                    title = strings.assignOwner,
                    icon = Icons.Outlined.Person
                ) {
                    OwnerDropdown(state, viewModel)
                }
            }

            val technicalError = state.showErrors && (state.description.isBlank() || state.capacity.isBlank() || state.pricePerHour.isBlank())

            PremiumCard(
                title = strings.technicalInfo,
                icon = Icons.Outlined.Info,
                isError = technicalError
            ) {
                LabelAndField(
                    "${strings.description} (ixtiyoriy)",
                    state.description,
                    "Stadion haqida qo'shimcha ma'lumot...",
                    singleLine = false,
                    icon = Icons.Outlined.Description,
                    isError = false
                ) {
                    viewModel.handleEvent(AddStadiumContract.Event.Description(it))
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
                            icon = Icons.Outlined.GridView,
                            isError = state.showErrors && state.capacity.isBlank()
                        ) {
                            viewModel.handleEvent(AddStadiumContract.Event.Capacity(it))
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
                    icon = Icons.Outlined.Payments,
                    visualTransformation = AmountTransformation(),
                    isError = state.showErrors && state.pricePerHour.isBlank()
                ) {
                    viewModel.handleEvent(AddStadiumContract.Event.PricePerHour(it))
                }
            }

            PremiumCard(
                title = "Xaritada joylashuv",
                icon = Icons.Outlined.Map
            ) {
                LocationPicker(
                    latitude = state.latitude,
                    longitude = state.longitude,
                    address = state.preciseAddress,
                    onLatitudeChange = { viewModel.handleEvent(AddStadiumContract.Event.Latitude(it.toDoubleOrNull())) },
                    onLongitudeChange = { viewModel.handleEvent(AddStadiumContract.Event.Longitude(it.toDoubleOrNull())) },
                    onAddressChange = { viewModel.handleEvent(AddStadiumContract.Event.PreciseAddress(it)) },
                    onSelectOnMap = {
                        navigator.push(MapSelectionScreen(state.latitude, state.longitude) { lat, lng ->
                            viewModel.handleEvent(AddStadiumContract.Event.Latitude(lat))
                            viewModel.handleEvent(AddStadiumContract.Event.Longitude(lng))
                        })
                    }
                )
            }

            PremiumCard(
                title = strings.workingHoursAndImages,
                icon = Icons.Outlined.Schedule
            ) {
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
                    onClick = { /* TODO: Image picker */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Outlined.AddAPhoto, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.addPhoto, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.handleEvent(AddStadiumContract.Event.Save) },
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
                    Icon(Icons.Outlined.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.save.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun PremiumCard(
    title: String,
    icon: ImageVector,
    isError: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isError) BorderStroke(1.dp, MaterialTheme.colorScheme.error) 
                 else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            null,
                            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
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
    state: AddStadiumContract.State,
    viewModel: AddStadiumViewModel,
    isError: Boolean = false
) {
    val strings = Localization.current
    Column {
        Text(strings.chooseRegion.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showRegionDropdown,
            onExpandedChange = { viewModel.handleEvent(AddStadiumContract.Event.ShowRegionDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedRegion?.name ?: strings.chooseRegion,
                onValueChange = {},
                readOnly = true,
                isError = isError,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showRegionDropdown) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            ExposedDropdownMenu(
                expanded = state.showRegionDropdown,
                onDismissRequest = { viewModel.handleEvent(AddStadiumContract.Event.ShowRegionDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                state.regions.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(region.name) },
                        onClick = {
                            viewModel.handleEvent(AddStadiumContract.Event.SelectRegion(region))
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
    state: AddStadiumContract.State,
    viewModel: AddStadiumViewModel,
    isError: Boolean = false
) {
    val strings = Localization.current
    Column {
        Text(strings.chooseDistrict.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showDistrictDropdown,
            onExpandedChange = { viewModel.handleEvent(AddStadiumContract.Event.ShowDistrictDropdown(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedDistrict?.name ?: strings.chooseDistrict,
                onValueChange = {},
                readOnly = true,
                isError = isError,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDistrictDropdown) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            ExposedDropdownMenu(
                expanded = state.showDistrictDropdown,
                onDismissRequest = { viewModel.handleEvent(AddStadiumContract.Event.ShowDistrictDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                state.districts.forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district.name ?: "") },
                        onClick = {
                            viewModel.handleEvent(AddStadiumContract.Event.SelectDistrict(district))
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
    state: AddStadiumContract.State,
    viewModel: AddStadiumViewModel
) {
    val strings = Localization.current
    Column {
        Text(strings.assignOwner.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showOwnerDropdown,
            onExpandedChange = { viewModel.handleEvent(AddStadiumContract.Event.ShowOwnerDropdown(it)) },
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
                onDismissRequest = { viewModel.handleEvent(AddStadiumContract.Event.ShowOwnerDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                state.owners.forEach { owner ->
                    DropdownMenuItem(
                        text = { 
                            Text("${owner.id} | ${owner.fullName ?: owner.username ?: ""}") 
                        },
                        onClick = {
                            viewModel.handleEvent(AddStadiumContract.Event.SelectOwner(owner))
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
    state: AddStadiumContract.State,
    viewModel: AddStadiumViewModel
) {
    val strings = Localization.current
    Column {
        Text(strings.sportType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.showTypeDropdown,
            onExpandedChange = { viewModel.handleEvent(AddStadiumContract.Event.ShowTypeDropdown(it)) },
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
                onDismissRequest = { viewModel.handleEvent(AddStadiumContract.Event.ShowTypeDropdown(false)) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                StadiumType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = when(type) {
                                    StadiumType.FOOTBALL -> Icons.Outlined.SportsSoccer
                                    StadiumType.TENNIS -> Icons.Outlined.SportsTennis
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            viewModel.handleEvent(AddStadiumContract.Event.Type(type))
                            viewModel.handleEvent(AddStadiumContract.Event.ShowTypeDropdown(false))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish", color = MaterialTheme.colorScheme.secondary) }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    )
}
