package uz.coder.foottopbusiness.presentation.main.coaches

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object CoachesVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<CoachesViewModel>()
        CoachesScreen(viewModel)
    }
}
