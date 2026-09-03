package uz.coder.foottopbusiness.presentation.main.stadium.addstadium

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel

object AddStadiumVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<AddStadiumViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        AddStadiumScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() }
        )
    }
}
