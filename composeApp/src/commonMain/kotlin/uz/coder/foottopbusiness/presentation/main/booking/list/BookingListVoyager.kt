package uz.coder.foottopbusiness.presentation.main.booking.list

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject

/**
 * @param isRoot murabbiy uchun bu ekran pastki panel tab'i sifatida ochiladi -
 *   u holda qaytadigan joy yo'q, orqaga tugmasi ham ko'rsatilmaydi
 */
class BookingListVoyager(private val isRoot: Boolean = false) : Screen {
    @Composable
    override fun Content() {
        val viewModel: BookingListViewModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        BookingListScreen(
            viewModel = viewModel,
            onBack = if (isRoot) null else ({ navigator.pop() })
        )
    }
}
