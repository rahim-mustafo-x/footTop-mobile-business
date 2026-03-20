package uz.coder.foottopbusiness.presentation.main.home

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

sealed interface HomeContract {
    data class State(
        // stadiums
        val stadiums: List<StadiumResponse> = emptyList(),
        val isLoadingStadiums: Boolean = false,
        val stadiumError: String? = null,
        val searchQuery: String = "",
        val filterActive: Boolean? = null,
        val currentPage: Int = 0,
        val isLastPage: Boolean = false,
        val deletingId: Int? = null,
        // tournaments
        val tournaments: List<TournamentResponseDto> = emptyList(),
        val isLoadingTournaments: Boolean = false,
        val selectedTournament: TournamentResponseDto? = null,
        // matches
        val matches: List<MatchResponseDto> = emptyList(),
        val isLoadingMatches: Boolean = false,
        val selectedMatch: MatchResponseDto? = null,
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object Refresh : Event
        data class Search(val query: String) : Event
        data class FilterActive(val isActive: Boolean?) : Event
        object LoadNextPage : Event
        data class DeleteRequest(val id: Int) : Event
        object DeleteConfirm : Event
        object DeleteCancel : Event
        data class SelectTournament(val t: TournamentResponseDto) : Event
        object ClearTournament : Event
        data class SelectMatch(val m: MatchResponseDto) : Event
        object ClearMatch : Event
    }
}
