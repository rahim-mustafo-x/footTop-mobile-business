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
                    sessionManager.networkError.collect { code ->
                        val message = when (code) {
                            401 -> "Sessiya muddati tugadi. Iltimos, qayta kiring."
                            403, 500 -> {
                                scope.launch {
                                    sessionManager.logout()
                                    navigator.replaceAll(LoginVoyager())
                                }
                                if (code == 403) "Kirish taqiqlangan (403)." else "Serverda xatolik yuz berdi (500)."
                            }
                            else -> "Tarmoq xatosi: $code"
                        }
                        snackbarHostState.showSnackbar(message)
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
