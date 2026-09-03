package uz.coder.foottopbusiness.presentation.main.stadium.edit

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel
import org.koin.core.parameter.parametersOf
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

data class EditStadiumVoyager(val stadium: StadiumResponse) : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<EditStadiumViewModel> { parametersOf(stadium) }
        val navigator = LocalNavigator.currentOrThrow
        EditStadiumScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() }
        )
    }
}
