package uz.coder.foottopbusiness.presentation.auth.otp

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.auth.login.LoginVoyager
import uz.coder.foottopbusiness.presentation.main.MainVoyager

object SendOtpVoyager: Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<SendOtpViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val snackBarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        viewModel.handleEvent(SendOtpContract.Event.Load)
        Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
            SendOtpScreen(navigateToLogin = {phoneNumber->
                navigator.push(LoginVoyager(phoneNumber))
            },
            showToast = {text->
                    scope.launch {
                        snackBarHostState.showSnackbar(text)
                    }
                },
            viewModel = viewModel)
        }
        LaunchedEffect(Unit){
            viewModel.effect.collect{
                if (it is SendOtpContract.Effect.Logged){
                    if (it.logged){
                        navigator.push(MainVoyager)
                    }
                }
            }
        }
    }
}