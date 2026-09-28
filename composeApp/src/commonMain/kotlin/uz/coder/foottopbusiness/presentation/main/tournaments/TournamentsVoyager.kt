package uz.coder.foottopbusiness.presentation.main.tournaments

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

object TournamentsVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<TournamentsViewModel>()
        TournamentsScreen(viewModel)
    }
}
