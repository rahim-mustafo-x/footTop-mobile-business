package uz.coder.foottopbusiness.presentation.main.home.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.collectLatest
import uz.coder.foottopbusiness.core.BackHandler

import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.presentation.main.settings.SettingsViewModel

import uz.coder.foottopbusiness.core.visualTransformation.PhoneTransformation
import uz.coder.foottopbusiness.core.visualTransformation.AmountTransformation

class UserCreateScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<UserCreateViewModel>()
        val state by viewModel.state.collectAsState()
        val settingsViewModel = koinInject<SettingsViewModel>()
        val userState by settingsViewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val strings = Localization.current

        val currentUserRole = userState.user?.roles?.firstOrNull()?.name ?: ""
        val isSuperAdmin = currentUserRole == "ROLE_SUPER_ADMIN"
        val isDistrictAdmin = currentUserRole == "ROLE_DISTRICT_ADMIN"
        val isPrivileged = isSuperAdmin || isDistrictAdmin

        val availableRoles = remember(isSuperAdmin, isDistrictAdmin, currentUserRole) {
            val roles = mutableListOf<Triple<String, String, String>>()
            
            if (isSuperAdmin) {
                roles.add(Triple("ROLE_DISTRICT_ADMIN", "Tuman Admini", "Tuman bo'yicha boshqaruv"))
            }
            
            if (isPrivileged) {
                roles.add(Triple("ROLE_OWNER", "Stadion egasi", "Maydonlarni nazorat"))
            }

            if (isPrivileged || currentUserRole == "ROLE_OWNER") {
                roles.add(Triple("ROLE_COACH", "Coach", "Murabbiy kabineti"))
            }

            if (isPrivileged) {
                roles.add(Triple("ROLE_PLAYER", "O'yinchi", "O'yinlar ishtirokchisi"))
            }
            roles
        }

        LaunchedEffect(userState.isLoadingUser) {
            if (!userState.isLoadingUser && !isPrivileged && currentUserRole != "ROLE_OWNER") {
                navigator.pop()
            }
        }

        BackHandler {
            navigator.pop()
        }

        LaunchedEffect(Unit) {
            viewModel.onEvent(UserCreateContract.Event.LoadInitialData)
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is UserCreateContract.Effect.ShowError -> {
                        snackbarHostState.showSnackbar(ErrorMapper.map(effect.message, strings))
                    }
                    UserCreateContract.Effect.NavigateBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        Scaffold(
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(Modifier.width(16.dp))
                        val title = when(state.role) {
                            "ROLE_PLAYER" -> strings.addPlayer
                            "ROLE_DISTRICT_ADMIN" -> strings.addDistrictAdmin
                            "ROLE_COACH" -> strings.addCoach
                            else -> strings.createUser
                        }
                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.surface
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(LayoutDirection.Ltr), end = padding.calculateEndPadding(
                        LayoutDirection.Ltr))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(strings.selectRole, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(12.dp))
                    
                    availableRoles.chunked(2).forEach { rowRoles ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowRoles.forEach { role ->
                                val icon = when (role.first) {
                                    "ROLE_DISTRICT_ADMIN" -> Icons.Default.Settings
                                    "ROLE_OWNER" -> Icons.Outlined.Home
                                    "ROLE_COACH" -> Icons.Outlined.Person
                                    "ROLE_PLAYER" -> Icons.Default.SportsSoccer
                                    else -> Icons.Outlined.Visibility
                                }
                                RoleItem(
                                    title = role.second,
                                    icon = icon,
                                    subtitle = role.third,
                                    isSelected = state.role == role.first,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    viewModel.onEvent(UserCreateContract.Event.RoleChanged(role.first))
                                }
                            }
                            if (rowRoles.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (state.role == "ROLE_COACH") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(strings.user.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box {
                            OutlinedTextField(
                                value = state.selectedUser?.let { "${it.id} | ${it.fullName ?: it.username}" } ?: strings.chooseDistrict,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showUserDropdown) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { viewModel.onEvent(UserCreateContract.Event.ShowUserDropdown(true)) }
                            )
                            DropdownMenu(
                                expanded = state.showUserDropdown,
                                onDismissRequest = { viewModel.onEvent(UserCreateContract.Event.ShowUserDropdown(false)) },
                                modifier = Modifier.fillMaxWidth(0.9f).background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.newEmployee) },
                                    onClick = { viewModel.onEvent(UserCreateContract.Event.UserSelected(null)) }
                                )
                                state.users.forEach { user ->
                                    DropdownMenuItem(
                                        text = { Text("${user.id} | ${user.fullName ?: user.username}") },
                                        onClick = { viewModel.onEvent(UserCreateContract.Event.UserSelected(user)) }
                                    )
                                }
                            }
                        }
                    }
                }

                LabelAndField(strings.fullName, state.fullName, strings.enterName) {
                    viewModel.onEvent(UserCreateContract.Event.FullNameChanged(it))
                }

                LabelAndField(strings.loginEmail, state.login, strings.loginEmailPlaceholder) {
                    viewModel.onEvent(UserCreateContract.Event.LoginChanged(it))
                }

                LabelAndField(strings.phoneNumber, state.phone, "901234567", KeyboardType.Phone, visualTransformation = PhoneTransformation()) {
                    if (it.length <= 9) {
                        viewModel.onEvent(UserCreateContract.Event.PhoneChanged(it))
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(strings.password, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(
                            onClick = { viewModel.onEvent(UserCreateContract.Event.GeneratePasswordClicked) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.randomPassword, fontSize = 12.sp)
                        }
                    }
                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(UserCreateContract.Event.PasswordChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(strings.passwordPlaceholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                if (state.role == "ROLE_COACH") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(strings.coachProfile.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.2.sp)
                            
                            LabelAndField(strings.specialty, state.specialty, "Football") {
                                viewModel.onEvent(UserCreateContract.Event.SpecialtyChanged(it))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LabelAndField(strings.experience, state.experience, "5", KeyboardType.Number, modifier = Modifier.weight(1f)) {
                                    viewModel.onEvent(UserCreateContract.Event.ExperienceChanged(it))
                                }
                                LabelAndField(strings.price, state.hourlyRate, "150000", KeyboardType.Number, modifier = Modifier.weight(1f), visualTransformation = AmountTransformation()) {
                                    viewModel.onEvent(UserCreateContract.Event.HourlyRateChanged(it))
                                }
                            }

                            LabelAndField(strings.availability, state.availability, "Mon-Fri 18:00") {
                                viewModel.onEvent(UserCreateContract.Event.AvailabilityChanged(it))
                            }
                        }
                    }
                }

                if (state.role == "ROLE_DISTRICT_ADMIN" || state.role == "ROLE_OWNER") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(strings.chooseArea, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        
                        var regionExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedTextField(
                                value = state.selectedRegion?.name ?: strings.chooseRegion,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(strings.location) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { regionExpanded = true }
                            )
                            DropdownMenu(
                                expanded = regionExpanded,
                                onDismissRequest = { regionExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                state.regions.forEach { region ->
                                    DropdownMenuItem(
                                        text = { Text(region.name ?: "") },
                                        onClick = {
                                            viewModel.onEvent(UserCreateContract.Event.RegionSelected(region))
                                            regionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var districtExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedTextField(
                                value = state.selectedDistrict?.name ?: strings.chooseDistrict,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(strings.location) },
                                enabled = state.selectedRegion != null,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(enabled = state.selectedRegion != null) { districtExpanded = true }
                            )
                            DropdownMenu(
                                expanded = districtExpanded,
                                onDismissRequest = { districtExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                state.districts.forEach { district ->
                                    DropdownMenuItem(
                                        text = { Text(district.name ?: "") },
                                        onClick = {
                                            viewModel.onEvent(UserCreateContract.Event.DistrictSelected(district))
                                            districtExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(strings.smsHint, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.onEvent(UserCreateContract.Event.CreateClicked) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        val btnText = when(state.role) {
                            "ROLE_PLAYER" -> strings.addPlayer
                            "ROLE_COACH" -> strings.addCoach
                            "ROLE_DISTRICT_ADMIN" -> strings.addDistrictAdmin
                            else -> strings.save
                        }
                        Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
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
        visualTransformation: VisualTransformation = VisualTransformation.None,
        modifier: Modifier = Modifier,
        onValueChange: (String) -> Unit
    ) {
        Column(modifier = modifier) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }

    @Composable
    private fun RoleItem(
        title: String,
        icon: ImageVector,
        subtitle: String,
        isSelected: Boolean,
        modifier: Modifier,
        onClick: () -> Unit
    ) {
        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

        Card(
            modifier = modifier
                .height(100.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    subtitle,
                    fontSize = 10.sp,
                    color = if (isSelected) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
