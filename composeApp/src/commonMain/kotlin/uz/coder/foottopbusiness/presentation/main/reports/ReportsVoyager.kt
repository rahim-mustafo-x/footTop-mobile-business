package uz.coder.foottopbusiness.presentation.main.reports

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

/** "Hisobot" tabining ildiz ekrani. */
object ReportsVoyager : Screen {
    @Composable
    override fun Content() {
        ReportsScreen(getScreenModel<ReportsViewModel>())
    }
}
