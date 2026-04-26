package uz.coder.foottopbusiness.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.presentation.auth.login.LoginVoyager
import uz.coder.foottopbusiness.presentation.splash.SplashVoyager

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavigation() {
    val sessionManager = koinInject<SessionManager>()
    val isTokenValid by sessionManager.isTokenValid.collectAsState(initial = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showServerErrorDialog by remember { mutableStateOf(false) }
    var serverErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        sessionManager.startObservingToken(this)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (showServerErrorDialog) {
            AlertDialog(
                onDismissRequest = { showServerErrorDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Server Xatoligi",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = serverErrorMessage)
                },
                confirmButton = {
                    Button(
                        onClick = { showServerErrorDialog = false }
                    ) {
                        Text("Tushunarli")
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Navigator(SplashVoyager) { navigator ->
                LaunchedEffect(Unit) {
                    sessionManager.networkError.collect { error ->
                        val baseMessage = when (error.code) {
                            401 -> "Sessiya muddati tugadi. Iltimos, qayta kiring."
                            403 -> "Sizda bu amalni bajarish uchun ruxsat yo'q (403)"
                            404 -> "Ma'lumot topilmadi (404)"
                            409 -> "Bunday ma'lumot allaqachon mavjud yoki ziddiyat yuzaga keldi (409)"
                            400 -> "Xato so'rov yuborildi (400)"
                            500 -> "Serverda xatolik yuz berdi (500)"
                            else -> "Tarmoq xatosi: ${error.code}"
                        }
                        val displayMessage = when {
                            !error.details.isNullOrEmpty() -> {
                                error.details.joinToString("\n") { detail ->
                                    if (detail.contains(":")) detail.substringAfter(":").trim() else detail
                                }
                            }
                            !error.message.isNullOrBlank() -> {
                                if (error.message.contains(":")) error.message.substringAfter(":").trim() else error.message
                            }
                            else -> baseMessage
                        }
                        
                        if (error.code == 500) {
                            serverErrorMessage = displayMessage
                            showServerErrorDialog = true
                        } else {
                            snackbarHostState.showSnackbar(displayMessage)
                        }
                    }
                }

    LaunchedEffect(isTokenValid) {
        if (!isTokenValid) {
            sessionManager.startObservingToken(scope)
            if (navigator.lastItem !is LoginVoyager) {
                navigator.replaceAll(LoginVoyager())
            }
        }
    }
                navigator.lastItem.Content()
            }
        }
    }
}
