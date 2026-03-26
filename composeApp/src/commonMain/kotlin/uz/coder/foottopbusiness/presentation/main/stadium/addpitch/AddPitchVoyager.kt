package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject

object AddPitchVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<AddPitchViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        AddPitchScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() }
        )
    }
}
