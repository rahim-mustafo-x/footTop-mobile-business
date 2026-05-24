package uz.coder.foottopbusiness.presentation.main.booking.list

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject

class BookingListVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel: BookingListViewModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        BookingListScreen(viewModel = viewModel, onBack = { navigator.pop() })
    }
}
