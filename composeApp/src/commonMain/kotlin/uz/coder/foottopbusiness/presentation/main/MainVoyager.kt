package uz.coder.foottopbusiness.presentation.main

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object MainVoyager: Screen {
    @Composable
    override fun Content() {
        MainScreen()
    }
}