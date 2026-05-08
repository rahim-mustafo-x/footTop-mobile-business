package uz.coder.foottopbusiness.presentation.main.stadium.addstadium

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject

object AddStadiumVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<AddStadiumViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        AddStadiumScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() }
        )
    }
}
