package uz.coder.foottopbusiness.presentation.main.settings

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object SettingsVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<SettingsViewModel>()
        SettingsScreen(viewModel)
    }
}
