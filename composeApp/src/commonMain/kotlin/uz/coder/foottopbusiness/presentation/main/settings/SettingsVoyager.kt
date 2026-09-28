package uz.coder.foottopbusiness.presentation.main.settings

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

object SettingsVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<SettingsViewModel>()
        SettingsScreen(viewModel)
    }
}
