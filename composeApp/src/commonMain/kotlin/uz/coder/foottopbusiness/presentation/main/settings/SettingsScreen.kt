package uz.coder.foottopbusiness.presentation.main.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpVoyager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val state by viewModel.state.collectAsState()
    var showLogoutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsContract.Effect.NavigateToAuth -> navigator.replaceAll(SendOtpVoyager)
            }
        }
    }

    if (showLogoutSheet) {
        ModalBottomSheet(onDismissRequest = { showLogoutSheet = false }, sheetState = sheetState) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    .navigationBarsPadding().padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text("Chiqish", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Hisobingizdan chiqmoqchimisiz?", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showLogoutSheet = false; viewModel.handleEvent(SettingsContract.Event.Logout) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Ha, chiqish", fontSize = 15.sp) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showLogoutSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Bekor qilish", fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Sozlamalar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(20.dp))

        // Profil kartasi
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(2.dp)) {
            if (state.isLoadingUser) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Primary)
                }
            } else {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).background(Primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(state.user?.fullName ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(state.user?.phone ?: "—", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!state.user?.username.isNullOrBlank()) {
                            Text("@${state.user?.username}", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!state.user?.districtName.isNullOrBlank()) {
                            Text(state.user?.districtName ?: "", fontSize = 12.sp, color = Primary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()

        // Chiqish
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
                .let { mod -> mod.also { } },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
            TextButton(onClick = { showLogoutSheet = true }, modifier = Modifier.weight(1f)) {
                Text("Chiqish", fontSize = 15.sp, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth())
            }
        }
        HorizontalDivider()
    }
}
