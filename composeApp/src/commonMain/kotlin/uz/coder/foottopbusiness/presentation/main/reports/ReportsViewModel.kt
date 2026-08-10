package uz.coder.foottopbusiness.presentation.main.reports

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.admin.DashboardUseCase
import uz.coder.foottopbusiness.domain.usecase.admin.WeeklyReportUseCase
import uz.coder.foottopbusiness.domain.usecase.match.GetMatchesUseCase

/**
 * Hisobot ekranining o'z ViewModel'i.
 *
 * Ilgari ReportsScreen HomeViewModel'ni koinInject qilardi. HomeViewModel esa
 * DI'da `factory` - ya'ni Hisobot tab'i ochilganda ikkinchi nusxa yaratilib,
 * bosh sahifa allaqachon yuklab bo'lgan barcha so'rovlarni qaytadan yuborardi.
 * Endi bu ekran faqat o'ziga kerakli uchta so'rovni qiladi.
 */
class ReportsViewModel(
    private val dashboardUseCase: DashboardUseCase,
    private val weeklyReportUseCase: WeeklyReportUseCase,
    private val getMatchesUseCase: GetMatchesUseCase,
) : BaseViewModel<ReportsContract.State, ReportsContract.Effect, ReportsContract.Event>(
    initialState = ReportsContract.State()
) {
    init {
        handleEvent(ReportsContract.Event.Load)
    }

    override fun handleEvent(event: ReportsContract.Event) {
        when (event) {
            ReportsContract.Event.Load -> {
                loadDashboard()
                loadWeeklyReport()
                loadMatches()
            }

            ReportsContract.Event.DownloadReport -> downloadReport()
        }
    }

    private fun loadDashboard() {
        updateState { copy(isLoadingDashboard = true) }
        executeAsync(
            onError = { updateState { copy(isLoadingDashboard = false) } }
        ) {
            dashboardUseCase().collect { dashboard ->
                updateState { copy(dashboard = dashboard, isLoadingDashboard = false) }
            }
        }
    }

    private fun loadWeeklyReport() {
        updateState { copy(isLoadingWeeklyReport = true) }
        executeAsync(
            onError = { updateState { copy(isLoadingWeeklyReport = false) } }
        ) {
            weeklyReportUseCase().collect { report ->
                updateState {
                    copy(
                        weeklyReport = report,
                        isLoadingWeeklyReport = false,
                        weeklyEarnings = report.dailyRevenue.map { it.revenue },
                        weeklyLabels = report.dailyRevenue.map { day ->
                            val parts = day.date.split("-")
                            if (parts.size == 3) "${parts[2]}.${parts[1]}" else day.date
                        }
                    )
                }
            }
        }
    }

    private fun loadMatches() {
        updateState { copy(isLoadingMatches = true) }
        executeAsync(
            onError = { updateState { copy(isLoadingMatches = false) } }
        ) {
            getMatchesUseCase().collect { matches ->
                updateState { copy(matches = matches, isLoadingMatches = false) }
            }
        }
    }

    private fun downloadReport() {
        executeAsync {
            val today = kotlin.time.Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

            val csv = StringBuilder()
            csv.append("ID,Sana,Nomi,Stadion ID,O'yinchilar,Narx,Jami\n")
            state.value.matches.forEach { match ->
                val total = (match.currentPlayers ?: 0) * (match.pricePerPlayer ?: 0.0)
                csv.append("${match.id},${match.dateTime},${match.title},${match.stadiumId},${match.currentPlayers},${match.pricePerPlayer},$total\n")
            }
            sendEffect(ReportsContract.Effect.DownloadFile("hisobot_$today.csv", csv.toString()))
        }
    }
}
