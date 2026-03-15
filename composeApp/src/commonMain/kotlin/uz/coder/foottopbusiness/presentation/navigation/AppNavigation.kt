package uz.coder.foottopbusiness.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import uz.coder.foottopbusiness.core.BackHandler
import uz.coder.foottopbusiness.core.exitApp
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpVoyager
import uz.coder.foottopbusiness.presentation.main.MainVoyager

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavigation(){
    Box(modifier = Modifier.fillMaxSize()){
        Navigator(SendOtpVoyager){navigator ->
            BackHandler(enabled = true){
                val screen = navigator.lastItem
                if (screen is MainVoyager){
                    exitApp()
                }else{
                    navigator.pop()
                }
            }
            navigator.lastItem.Content()
        }
    }
}