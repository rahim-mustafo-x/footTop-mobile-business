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
                updateState { copy(isLoading = true, error = null, page = 0, tournaments = emptyList(), isLastPage = false) }
                loadTournaments(0)
            }
            TournamentsContract.Event.LoadMore -> {
                val s = state.value
                if (!s.isLastPage && !s.isLoading && !s.isMoreLoading) {
                    loadTournaments(s.page + 1)
                }
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

    private fun loadTournaments(page: Int) {
        executeAsync(
            onLoading = { 
                if (page == 0) updateState { copy(isLoading = true) }
                else updateState { copy(isMoreLoading = true) }
            },
            block = {
                var result: uz.coder.foottopbusiness.data.network.dto.tournament.PageTournamentResponseDto? = null
                getTournamentsUseCase(page = page).collect { result = it }
                result!!
            },
            onSuccess = { pageData ->
                val newItems = pageData.content ?: emptyList()
                updateState {
                    copy(
                        tournaments = if (page == 0) newItems else tournaments + newItems,
                        page = page,
                        isLastPage = pageData.last ?: true,
                        isLoading = false,
                        isMoreLoading = false
                    )
                }
            },
            onError = {
                updateState { copy(error = it.message, isLoading = false, isMoreLoading = false) }
            }
        )
    }
}
