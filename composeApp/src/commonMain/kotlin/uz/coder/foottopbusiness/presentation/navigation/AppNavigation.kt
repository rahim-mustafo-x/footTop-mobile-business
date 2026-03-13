package uz.coder.foottopbusiness.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpVoyager

@Composable
fun AppNavigation(){
    Box(modifier = Modifier.fillMaxSize()){
        Navigator(SendOtpVoyager())
    }
}