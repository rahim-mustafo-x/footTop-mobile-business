package uz.coder.foottopbusiness.presentation.main.settings.notification

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

object SendNotificationVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<SendNotificationViewModel>()
        SendNotificationScreen(viewModel)
    }
}
