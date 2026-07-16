package uz.coder.foottopbusiness.presentation.main.home.slots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

class SlotsControlVoyager(private val stadium: StadiumResponse) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<HomeViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(stadium) {
            viewModel.handleEvent(HomeContract.Event.SelectStadiumForSlots(stadium))
        }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                if (effect is HomeContract.Effect.NavigateBack) {
                    navigator.pop()
                }
            }
        }

        SlotsControlScreen(stadium, state, viewModel)
    }
}