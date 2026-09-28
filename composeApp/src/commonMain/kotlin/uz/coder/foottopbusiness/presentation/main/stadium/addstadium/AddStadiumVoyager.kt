package uz.coder.foottopbusiness.presentation.main.stadium.addstadium

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel

class AddStadiumVoyager : Screen {
    override val key: ScreenKey = uniqueScreenKey

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
