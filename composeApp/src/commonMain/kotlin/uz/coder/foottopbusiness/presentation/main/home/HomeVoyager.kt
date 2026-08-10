package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.home.slots.SlotsControlVoyager

object HomeVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<HomeViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        // Effect oqimi Channel asosida (bitta iste'molchi) - uni HomeScreen
        // ichida collect qilamiz, bu yerda takrorlamaymiz.
        HomeScreen(
            viewModel = viewModel,
            navigateToSlotsControl = { stadium ->
                navigator.push(SlotsControlVoyager(stadium))
                viewModel.handleEvent(HomeContract.Event.ClearStadiumForSlots)
            }
        )
    }
}
