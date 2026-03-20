package uz.coder.foottopbusiness.presentation.main.tournaments

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto

sealed interface TournamentsContract {
    data class State(
        val tournaments: List<TournamentResponseDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedTournament: TournamentResponseDto? = null,
        val isCreating: Boolean = false,
        val showCreateDialog: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        data class Select(val tournament: TournamentResponseDto) : Event
        object ClearDetail : Event
        object ShowCreateDialog : Event
        object HideCreateDialog : Event
        data class Create(
            val name: String,
            val startDate: String,
            val endDate: String,
            val maxTeams: Int,
            val entryFee: Double,
            val address: String?,
        ) : Event
    }
}
