package uz.coder.foottopbusiness.presentation.main.tournaments

import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.usecase.tournament.CreateTournamentUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase

class TournamentsViewModel(
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val createTournamentUseCase: CreateTournamentUseCase,
    private val preferencesManager: PreferencesManager,
) : BaseViewModel<TournamentsContract.State, TournamentsContract.Effect, TournamentsContract.Event>(
    initialState = TournamentsContract.State()
) {
    init { handleEvent(TournamentsContract.Event.Load) }

    override fun handleEvent(event: TournamentsContract.Event) {
        when (event) {
            TournamentsContract.Event.Load -> {
                updateState { copy(isLoading = true, error = null) }
                executeAsync(
                    block = { var r = emptyList<TournamentResponseDto>(); getTournamentsUseCase().collect { r = it }; r },
                    onSuccess = { updateState { copy(tournaments = it, isLoading = false) } },
                    onError = { updateState { copy(error = it.message, isLoading = false) } }
                )
            }
            is TournamentsContract.Event.Select -> updateState { copy(selectedTournament = event.tournament) }
            TournamentsContract.Event.ClearDetail -> updateState { copy(selectedTournament = null) }
            TournamentsContract.Event.ShowCreateDialog -> updateState { copy(showCreateDialog = true) }
            TournamentsContract.Event.HideCreateDialog -> updateState { copy(showCreateDialog = false) }
            is TournamentsContract.Event.Create -> {
                updateState { copy(isCreating = true, showCreateDialog = false) }
                executeAsync(
                    block = {
                        val userId = preferencesManager.userId.firstOrNull() ?: 0L
                        var result: TournamentResponseDto? = null
                        createTournamentUseCase(
                            TournamentRequestDto(
                                name = event.name,
                                organizerId = userId.toLong(),
                                startDate = event.startDate,
                                endDate = event.endDate,
                                maxTeams = event.maxTeams,
                                entryFee = event.entryFee.toLong(),
                                address = event.address,
                                startTime = event.startTime,
                                endTime = event.endTime,
                            )
                        ).collect { result = it }
                        result!!
                    },
                    onSuccess = { created ->
                        updateState { copy(isCreating = false, tournaments = tournaments + created) }
                        sendEffect(TournamentsContract.Effect.ShowToast("Turnir yaratildi"))
                    },
                    onError = {
                        updateState { copy(isCreating = false) }
                        sendEffect(TournamentsContract.Effect.ShowToast(it.message ?: "Xatolik"))
                    }
                )
            }
        }
    }
}
