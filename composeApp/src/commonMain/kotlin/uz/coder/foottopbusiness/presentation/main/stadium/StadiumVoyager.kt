package uz.coder.foottopbusiness.presentation.main.stadium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchScreen
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchViewModel

object StadiumVoyager : Screen {
    @Composable
    override fun Content() {
        ContentWithNav()
    }

    @Composable
    fun ContentWithNav(onNavigateToAddPitch: (() -> Unit)? = null) {
        val viewModel = koinInject<StadiumViewModel>()
        StadiumScreen(
            viewModel = viewModel,
            onNavigateToAddPitch = { onNavigateToAddPitch?.invoke() }
        )
    }
}
