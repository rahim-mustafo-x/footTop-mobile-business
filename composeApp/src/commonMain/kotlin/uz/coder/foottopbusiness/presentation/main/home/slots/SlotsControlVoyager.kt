package uz.coder.foottopbusiness.presentation.main.home.slots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.home.HomeContract
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel

class SlotsControlVoyager(private val stadium: StadiumResponse) : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val viewModel = getScreenModel<HomeViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(stadium) {
            viewModel.handleEvent(HomeContract.Event.SelectStadiumForSlots(stadium))
        }

        // Effect oqimi Channel asosida (bitta iste'molchi) - uni SlotsControlScreen
        // ichida collect qilamiz, bu yerda takrorlamaymiz.
        SlotsControlScreen(stadium, state, viewModel)
    }
}
