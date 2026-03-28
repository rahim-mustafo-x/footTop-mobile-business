package uz.coder.foottopbusiness.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.SessionState
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpVoyager
import uz.coder.foottopbusiness.presentation.splash.SplashVoyager

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavigation() {
    val sessionManager = koinInject<SessionManager>()
    val sessionState by sessionManager.sessionState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Navigator(SplashVoyager) { navigator ->
            LaunchedEffect(Unit) {
                if (sessionState == SessionState.EXPIRED) {
                    navigator.replaceAll(SendOtpVoyager)
                }
            }
            navigator.lastItem.Content()
        }
    }
}
