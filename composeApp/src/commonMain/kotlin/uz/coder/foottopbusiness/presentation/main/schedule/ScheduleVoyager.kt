package uz.coder.foottopbusiness.presentation.main.schedule

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

/** Egasining "Jadval" tabining ildiz ekrani. */
object ScheduleVoyager : Screen {
    @Composable
    override fun Content() {
        ScheduleScreen(getScreenModel<ScheduleViewModel>())
    }
}
