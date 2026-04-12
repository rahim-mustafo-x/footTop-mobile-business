package uz.coder.foottopbusiness.presentation.main.settings.notification

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object SendNotificationVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<SendNotificationViewModel>()
        SendNotificationScreen(viewModel)
    }
}
