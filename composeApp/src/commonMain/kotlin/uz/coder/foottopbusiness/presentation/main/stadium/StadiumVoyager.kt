package uz.coder.foottopbusiness.presentation.main.stadium

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.stadium.details.StadiumDetailsScreen
import uz.coder.foottopbusiness.presentation.main.stadium.details.StadiumDetailsViewModel

object StadiumVoyager : Screen {
    @Composable
    override fun Content() {
        ContentWithNav()
    }

    @Composable
    fun ContentWithNav(onNavigateToAddPitch: (() -> Unit)? = null) {
        val viewModel = koinInject<StadiumViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        StadiumScreen(
            viewModel = viewModel,
            onNavigateToAddPitch = { onNavigateToAddPitch?.invoke() }
        )

        // Observe effects for navigation
        androidx.compose.runtime.LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is StadiumContract.Effect.NavigateToDetails -> {
                        navigator.push(StadiumDetailsVoyager(effect.stadium))
                    }
                    else -> {}
                }
            }
        }
    }
}

data class StadiumDetailsVoyager(val stadium: uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = StadiumDetailsViewModel(stadium)
        StadiumDetailsScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() }
        )
    }
}
