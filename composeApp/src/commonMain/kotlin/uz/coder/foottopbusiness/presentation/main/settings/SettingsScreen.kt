package uz.coder.foottopbusiness.presentation.main.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpVoyager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    var showLogoutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsContract.Effect.NavigateToAuth -> {
                    navigator.replaceAll(SendOtpVoyager)
                }
            }
        }
    }

    if (showLogoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Chiqish", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hisobingizdan chiqmoqchimisiz?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        showLogoutSheet = false
                        viewModel.handleEvent(SettingsContract.Event.Logout)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ha, chiqish", fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showLogoutSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bekor qilish", fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Sozlamalar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(20.dp))

        SettingsGroup("Profil") {
            SettingsItem(Icons.Default.Person, "Profil ma'lumotlari") {}
            SettingsItem(Icons.Default.Notifications, "Bildirishnomalar") {}
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup("Ilova") {
            SettingsItem(Icons.Default.Info, "Ilova haqida") {}
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup("Boshqa") {
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                label = "Chiqish",
                tint = MaterialTheme.colorScheme.error,
                onClick = { showLogoutSheet = true }
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp))
    HorizontalDivider()
    content()
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = Primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null,
            tint = tint, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 15.sp,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            color = if (tint == MaterialTheme.colorScheme.error) tint
                    else MaterialTheme.colorScheme.onSurface)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
    HorizontalDivider()
}
