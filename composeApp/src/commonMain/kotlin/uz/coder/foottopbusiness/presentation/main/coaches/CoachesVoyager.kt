package uz.coder.foottopbusiness.presentation.main.coaches

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

object CoachesVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<CoachesViewModel>()
        CoachesScreen(viewModel)
    }
}
