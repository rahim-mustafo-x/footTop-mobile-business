package uz.coder.foottopbusiness.presentation.main.tournaments

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object TournamentsVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<TournamentsViewModel>()
        TournamentsScreen(viewModel)
    }
}
