package uz.coder.foottopbusiness.presentation.main.reports

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.owner.OwnerReportDto

/** Hisobot davri: oxirgi 7 kun, oxirgi 30 kun yoki shu oy. */
enum class ReportPeriod { WEEK, DAYS_30, MONTH }

sealed interface ReportsContract {
    data class State(
        val period: ReportPeriod = ReportPeriod.DAYS_30,
        val from: String = "",
        val to: String = "",
        val isLoading: Boolean = true,
        val report: OwnerReportDto? = null,
        /** Xom xabar - ekranda ErrorMapper orqali o'giriladi. */
        val error: String? = null,
        /** Qaysi qarzdor bron "to'landi" deb belgilanyapti. */
        val payingId: Long? = null,
        val isExporting: Boolean = false
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowError(val message: String) : Effect
        object PaymentUpdated : Effect
        data class DownloadFile(val fileName: String, val content: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        data class SelectPeriod(val period: ReportPeriod) : Event
        data class MarkPaid(val bookingId: Long) : Event
        object DownloadReport : Event
    }
}
