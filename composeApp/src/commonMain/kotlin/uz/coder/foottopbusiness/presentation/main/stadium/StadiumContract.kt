package uz.coder.foottopbusiness.presentation.main.stadium

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

sealed interface StadiumContract {
    data class State(
        val stadiums: List<StadiumResponse> = emptyList(),
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val filterActive: Boolean? = null,
        val currentPage: Int = 0,
        val isLastPage: Boolean = false,
        val error: String? = null
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data class NavigateToDetails(val stadium: StadiumResponse) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object Refresh : Event
        data class Search(val query: String) : Event
        data class FilterActive(val isActive: Boolean?) : Event
        object LoadNextPage : Event
        data class StadiumClick(val stadium: StadiumResponse) : Event
    }
}
