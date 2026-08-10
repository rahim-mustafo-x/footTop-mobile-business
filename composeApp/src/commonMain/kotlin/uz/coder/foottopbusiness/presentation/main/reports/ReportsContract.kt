package uz.coder.foottopbusiness.presentation.main.reports

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.domain.model.Dashboard
import uz.coder.foottopbusiness.domain.model.WeeklyReport

sealed interface ReportsContract {
    data class State(
        val dashboard: Dashboard? = null,
        val isLoadingDashboard: Boolean = false,

        val weeklyReport: WeeklyReport? = null,
        val isLoadingWeeklyReport: Boolean = false,

        // Haftalik grafik uchun tayyorlangan ma'lumot.
        // DIQQAT: WeeklyRevenueChart hozircha chaqirilmayapti.
        val weeklyEarnings: List<Double> = emptyList(),
        val weeklyLabels: List<String> = emptyList(),

        val matches: List<MatchResponseDto> = emptyList(),
        val isLoadingMatches: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data class DownloadFile(val fileName: String, val content: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object DownloadReport : Event
    }
}
