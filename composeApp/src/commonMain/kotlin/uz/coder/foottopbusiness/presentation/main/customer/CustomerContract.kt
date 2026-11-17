package uz.coder.foottopbusiness.presentation.main.customer

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.booking.CustomerSummaryDto

sealed interface CustomerContract {
    data class State(
        val phone: String,
        val isLoading: Boolean = true,
        val summary: CustomerSummaryDto? = null,
        /** Xom xabar - ekranda ErrorMapper orqali o'giriladi. */
        val error: String? = null
    ) : MviState

    sealed interface Effect : MviEffect

    sealed interface Event : MviEvent {
        object Load : Event
    }
}
