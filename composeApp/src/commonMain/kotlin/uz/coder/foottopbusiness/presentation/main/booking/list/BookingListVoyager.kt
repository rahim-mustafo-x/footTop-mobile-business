package uz.coder.foottopbusiness.presentation.main.booking.list

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * @param isRoot murabbiy uchun bu ekran pastki panel tab'i sifatida ochiladi -
 *   u holda qaytadigan joy yo'q, orqaga tugmasi ham ko'rsatilmaydi
 */
class BookingListVoyager(private val isRoot: Boolean = false) : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val viewModel= getScreenModel<BookingListViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        BookingListScreen(
            viewModel = viewModel,
            onBack = if (isRoot) null else ({ navigator.pop() })
        )
    }
}
