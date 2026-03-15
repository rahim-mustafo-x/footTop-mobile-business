package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object HomeVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<HomeViewModel>()
        HomeScreen(viewModel)
    }
}
