package uz.coder.foottopbusiness.presentation.main.stadium.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.core.visualTransformation.AmountTransformation
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.TimePickerDialog

import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStadiumScreen(viewModel: EditStadiumViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val hostState = remember { SnackbarHostState() }
    val strings = Localization.current

    val descFocus = remember { FocusRequester() }
    val capacityFocus = remember { FocusRequester() }
    val priceFocus = remember { FocusRequester() }

    BackHandler { onBack() }

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
        topBar = {
            TopAppBar(
                title = { Text("Stadionni tahrirlash", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                "Stadion muqovasi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (state.imageUrl.isBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Rasm URL manzilini kiriting",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    var isError by remember(state.imageUrl) { mutableStateOf(false) }
                    var isLoading by remember(state.imageUrl) { mutableStateOf(true) }

                    AsyncImage(
                        model = state.imageUrl,
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onSuccess = { isLoading = false; isError = false },
                        onError = { isLoading = false; isError = true }
                    )
                    
                    if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    
                    Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.TopEnd) {
                        Surface(
                            color = (if (isError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (isError) Icons.Default.Error else Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isError) "URL noto'g'ri" else "Internetda ko'rinadi", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.imageUrl,
                onValueChange = { viewModel.handleEvent(EditStadiumContract.Event.ImageUrl(it)) },
                label = { Text("Rasm manzili (HTTP URL)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = { Text("https://...") }
            )
            
            Spacer(Modifier.height(24.dp))
            Text(
                "Stadion ma'lumotlari",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.handleEvent(EditStadiumContract.Event.Name(it)) },
                label = { Text("Stadion nomi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descFocus.requestFocus() })
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.phone,
                onValueChange = { viewModel.handleEvent(EditStadiumContract.Event.Phone(it)) },
                label = { Text("Telefon raqami") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(12.dp))

            if (state.userRole == UserRole.SUPER_ADMIN || state.userRole == UserRole.DISTRICT_ADMIN) {
                Text(
                    "Owner biriktirish",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = state.showOwnerDropdown,
                    onExpandedChange = { viewModel.handleEvent(EditStadiumContract.Event.ShowOwnerDropdown(it)) }
                ) {
                    OutlinedTextField(
                        value = state.selectedOwner?.fullName ?: state.selectedOwner?.username ?: "Tanlang (Ixtiyoriy)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showOwnerDropdown) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = state.showOwnerDropdown,
                        onDismissRequest = { viewModel.handleEvent(EditStadiumContract.Event.ShowOwnerDropdown(false)) }
                    ) {
                        state.owners.forEach { owner ->
                            DropdownMenuItem(
                                text = { Text(owner.fullName ?: owner.username ?: "") },
                                onClick = { viewModel.handleEvent(EditStadiumContract.Event.SelectOwner(owner)) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.handleEvent(EditStadiumContract.Event.Description(it)) },
                label = { Text("Tavsif") },
                modifier = Modifier.fillMaxWidth().focusRequester(descFocus),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = state.showRegionDropdown,
                        onExpandedChange = { viewModel.handleEvent(EditStadiumContract.Event.ShowRegionDropdown(it)) }
                    ) {
                        OutlinedTextField(
                            value = state.selectedRegion?.name ?: "Viloyat",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showRegionDropdown) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = state.showRegionDropdown,
                            onDismissRequest = { viewModel.handleEvent(EditStadiumContract.Event.ShowRegionDropdown(false)) }
                        ) {
                            state.regions.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region.name) },
                                    onClick = { viewModel.handleEvent(EditStadiumContract.Event.SelectRegion(region)) }
                                )
                            }
                        }
                    }
                }
                Box(Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = state.showDistrictDropdown,
                        onExpandedChange = { if (state.selectedRegion != null) viewModel.handleEvent(EditStadiumContract.Event.ShowDistrictDropdown(it)) }
                    ) {
                        OutlinedTextField(
                            value = state.selectedDistrict?.name ?: "Tuman",
                            onValueChange = {},
                            readOnly = true,
                            enabled = state.selectedRegion != null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDistrictDropdown) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = state.showDistrictDropdown && state.districts.isNotEmpty(),
                            onDismissRequest = { viewModel.handleEvent(EditStadiumContract.Event.ShowDistrictDropdown(false)) }
                        ) {
                            state.districts.forEach { district ->
                                DropdownMenuItem(
                                    text = { Text(district.name?:"") },
                                    onClick = { viewModel.handleEvent(EditStadiumContract.Event.SelectDistrict(district)) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.capacity,
                    onValueChange = { viewModel.handleEvent(EditStadiumContract.Event.Capacity(it)) },
                    label = { Text("Sig'imi") },
                    modifier = Modifier.weight(1f).focusRequester(capacityFocus),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { priceFocus.requestFocus() })
                )
                OutlinedTextField(
                    value = state.pricePerHour,
                    onValueChange = { viewModel.handleEvent(EditStadiumContract.Event.PricePerHour(it)) },
                    label = { Text("Narxi/soat") },
                    modifier = Modifier.weight(1f).focusRequester(priceFocus),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    visualTransformation = AmountTransformation()
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimePickerField("Ochilishi", state.openTime, Modifier.weight(1f)) {
                    showOpenTimePicker = true
                }
                TimePickerField("Yopilishi", state.closeTime, Modifier.weight(1f)) {
                    showCloseTimePicker = true
                }
            }
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.handleEvent(EditStadiumContract.Event.Save) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("O'zgarishlarni saqlash", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun TimePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Transparent, RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
