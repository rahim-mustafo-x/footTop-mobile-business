package uz.coder.foottopbusiness.presentation.main.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumVoyager
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsVoyager

object HomeVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<HomeViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        HomeScreen(viewModel)
        LaunchedEffect(Unit){
            viewModel.effect.collect {result->
                when(result){
                    HomeContract.Effect.Stadium -> navigator.push(StadiumVoyager)
                    HomeContract.Effect.Tournament -> navigator.push(TournamentsVoyager)
                    else -> {}
                }
            }
        }
    }
}
