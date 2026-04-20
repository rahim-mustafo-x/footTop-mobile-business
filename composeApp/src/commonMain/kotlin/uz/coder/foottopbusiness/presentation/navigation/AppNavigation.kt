package uz.coder.foottopbusiness.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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

    LaunchedEffect(Unit) {
        sessionManager.startObservingToken(this)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Navigator(SplashVoyager) { navigator ->
                LaunchedEffect(Unit) {
                    sessionManager.networkError.collect { error ->
                        val baseMessage = when (error.code) {
                            401 -> "Sessiya muddati tugadi. Iltimos, qayta kiring."
                            403, 500 -> {
                                scope.launch {
                                    sessionManager.logout()
                                    navigator.replaceAll(LoginVoyager())
                                }
                                if (error.code == 403) "Kirish taqiqlangan." else "Serverda xatolik yuz berdi."
                            }
                            else -> "Tarmoq xatosi"
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
                        snackbarHostState.showSnackbar(displayMessage)
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
