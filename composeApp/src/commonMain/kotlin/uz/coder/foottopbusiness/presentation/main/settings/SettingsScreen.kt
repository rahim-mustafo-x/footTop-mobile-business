package uz.coder.foottopbusiness.presentation.main.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.platform.getPlatform
import uz.coder.foottopbusiness.core.ui.shimmer
import uz.coder.foottopbusiness.presentation.auth.login.LoginVoyager
import uz.coder.foottopbusiness.presentation.main.settings.about.AboutAppVoyager
import uz.coder.foottopbusiness.presentation.main.settings.editprofile.EditProfileVoyager

import uz.coder.foottopbusiness.core.visualTransformation.formatPhoneNumber
import uz.coder.foottopbusiness.core.platform.NotificationPermissionLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val state by viewModel.state.collectAsState()
    var showLogoutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val uriHandler = LocalUriHandler.current
    val platform = getPlatform()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val strings = Localization.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsContract.Effect.NavigateToAuth -> navigator.replaceAll(LoginVoyager())
                SettingsContract.Effect.NavigateToAbout -> navigator.push(AboutAppVoyager)
                is SettingsContract.Effect.ShowToast -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }

    if (state.showChangePasswordDialog) {
        var oldPasswordVisible by remember { mutableStateOf(false) }
        var newPasswordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(SettingsContract.Event.DismissChangePassword) },
            title = { Text(strings.changePassword) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.oldPassword,
                        onValueChange = { viewModel.handleEvent(SettingsContract.Event.UpdateOldPassword(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.oldPassword) },
                        placeholder = { Text(strings.oldPasswordPlaceholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                Icon(
                                    imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = { viewModel.handleEvent(SettingsContract.Event.UpdateNewPassword(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.newPassword) },
                        placeholder = { Text(strings.newPasswordPlaceholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.handleEvent(SettingsContract.Event.UpdateConfirmPassword(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.confirmPassword) },
                        placeholder = { Text(strings.confirmPasswordPlaceholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.handleEvent(SettingsContract.Event.ChangePassword) },
                    enabled = !state.isChangingPassword
                ) {
                    if (state.isChangingPassword) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(strings.save)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(SettingsContract.Event.DismissChangePassword) }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (state.showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.handleEvent(SettingsContract.Event.DismissDeleteAccount) },
            title = { Text(strings.deleteAccountTitle, color = MaterialTheme.colorScheme.error) },
            text = {
                Column {
                    Text(strings.deleteConfirmText)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.deleteConfirmText,
                        onValueChange = { viewModel.handleEvent(SettingsContract.Event.UpdateDeleteConfirmText(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(state.user?.username ?: "") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.handleEvent(SettingsContract.Event.DeleteAccount) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !state.isDeleting && state.deleteConfirmText == state.user?.username
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(strings.delete)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(SettingsContract.Event.DismissDeleteAccount) }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showLogoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutSheet = !showLogoutSheet },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(strings.logoutConfirmTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    strings.logoutConfirmMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = {
                        showLogoutSheet = !showLogoutSheet
                        viewModel.handleEvent(SettingsContract.Event.Logout)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.yesLogout, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showLogoutSheet = false },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(strings.cancel, fontSize = 15.sp)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarPadding, start = 8.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
                Text(
                    strings.profileAndSettings,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Profil Kartasi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Dekorativ Background Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        if (state.isLoadingUser) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.size(90.dp).clip(CircleShape).shimmer())
                                Spacer(Modifier.height(16.dp))
                                Box(modifier = Modifier.width(150.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                                Spacer(Modifier.height(8.dp))
                                Box(modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Profil Rasmi / Icon
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        state.user?.fullName?.take(1)?.uppercase() ?: "B",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    state.user?.fullName ?: strings.user,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    formatPhoneNumber(state.user?.phone),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )

                                if (!state.user?.username.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        modifier = Modifier.padding(top = 12.dp)
                                    ) {
                                        Text(
                                            "@${state.user?.username}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bo'limlar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsSectionTitle(strings.personal)
                
                SettingsItem(
                    icon = Icons.Default.Edit,
                    iconTint = Color(0xFF4CAF50),
                    title = strings.editProfile,
                    subtitle = strings.editProfileSubtitle,
                    onClick = { navigator.push(EditProfileVoyager) }
                )

                SettingsItem(
                    icon = Icons.Default.Lock,
                    iconTint = Color(0xFF2196F3),
                    title = strings.changePassword,
                    subtitle = strings.changePasswordSubtitle,
                    onClick = { viewModel.handleEvent(SettingsContract.Event.ShowChangePassword) }
                )

                SettingsItem(
                    icon = Icons.Default.Notifications,
                    iconTint = Color(0xFF673AB7),
                    title = strings.notifications,
                    subtitle = if (state.notificationsEnabled) strings.active else strings.inactive,
                    onClick = { viewModel.handleEvent(SettingsContract.Event.CheckNotificationPermission) }
                )

                SettingsSectionTitle(strings.app)

                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    iconTint = Color(0xFFFF9800),
                    title = strings.helpAndContact,
                    subtitle = strings.helpAndContactSubtitle,
                    onClick = { uriHandler.openUri("https://t.me/rahim_mustafo_x") }
                )

                SettingsItem(
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFE91E63),
                    title = strings.rateApp,
                    subtitle = strings.rateAppSubtitle,
                    onClick = { uz.coder.foottopbusiness.core.platform.rateApp() }
                )

                SettingsItem(
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFF9C27B0),
                    title = strings.aboutApp,
                    subtitle = "${strings.version} ${platform.version}",
                    onClick = { viewModel.handleEvent(SettingsContract.Event.ShowAboutApp) }
                )

                Spacer(Modifier.height(8.dp))
                
                // Danger Zone
                SettingsSectionTitle(strings.danger)
                
                SettingsItem(
                    icon = Icons.Default.DeleteOutline,
                    iconTint = MaterialTheme.colorScheme.error,
                    title = strings.deleteAccountTitle,
                    subtitle = strings.deleteAccountSubtitle,
                    onClick = { viewModel.handleEvent(SettingsContract.Event.ShowDeleteAccount) }
                )

                // Chiqish
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { showLogoutSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            strings.logout,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (state.showNotificationPermissionDialog) {
        NotificationPermissionExplanationDialog(
            onConfirm = { viewModel.handleEvent(SettingsContract.Event.RequestNotificationPermission) },
            onDismiss = { viewModel.handleEvent(SettingsContract.Event.SetShowNotificationPermissionDialog(false)) }
        )
    }

    if (state.showPermanentlyDeniedDialog) {
        PermanentlyDeniedDialog(
            onOpenSettings = { viewModel.handleEvent(SettingsContract.Event.OpenSettings) },
            onDismiss = { viewModel.handleEvent(SettingsContract.Event.DismissPermanentlyDeniedDialog) }
        )
    }

    NotificationPermissionLauncher(
        trigger = state.triggerNotificationRequest,
        onResult = { status ->
            viewModel.handleEvent(SettingsContract.Event.OnNotificationPermissionResult(status))
        }
    )
}

@Composable
private fun NotificationPermissionExplanationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val strings = Localization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notificationRationaleTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.notificationRationaleDesc)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BenefitItem(strings.notificationBenefit1)
                    BenefitItem(strings.notificationBenefit2)
                    BenefitItem(strings.notificationBenefit3)
                    BenefitItem(strings.notificationBenefit4)
                }
                Text(
                    text = "${strings.enableNotifications}?",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.enableNotifications)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.maybeLater)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.Check, 
            null, 
            tint = Color(0xFF4CAF50), 
            modifier = Modifier.size(20.dp)
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PermanentlyDeniedDialog(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val strings = Localization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notificationsDeniedTitle) },
        text = { Text(strings.notificationsDeniedDesc) },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.openSettings)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
        letterSpacing = 1.5.sp
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
