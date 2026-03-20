package uz.coder.foottopbusiness.presentation.main.home

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.domain.usecase.match.GetMatchesUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.DeleteStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase

class HomeViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
    private val deleteStadiumUseCase: DeleteStadiumUseCase,
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val getMatchesUseCase: GetMatchesUseCase,
) : BaseViewModel<HomeContract.State, HomeContract.Effect, HomeContract.Event>(
    initialState = HomeContract.State()
) {
    init { handleEvent(HomeContract.Event.Load) }

    override fun handleEvent(event: HomeContract.Event) {
        when (event) {
            HomeContract.Event.Load, HomeContract.Event.Refresh -> {
                updateState { copy(currentPage = 0, stadiums = emptyList(), isLastPage = false) }
                loadStadiums(0)
                loadTournaments()
                loadMatches()
            }
            is HomeContract.Event.Search -> {
                updateState { copy(searchQuery = event.query, currentPage = 0, stadiums = emptyList()) }
                loadStadiums(0)
            }
            is HomeContract.Event.FilterActive -> {
                updateState { copy(filterActive = event.isActive, currentPage = 0, stadiums = emptyList()) }
                loadStadiums(0)
            }
            HomeContract.Event.LoadNextPage -> {
                val s = state.value
                if (!s.isLastPage && !s.isLoadingStadiums) loadStadiums(s.currentPage + 1)
            }
            is HomeContract.Event.DeleteRequest -> updateState { copy(deletingId = event.id) }
            HomeContract.Event.DeleteCancel -> updateState { copy(deletingId = null) }
            HomeContract.Event.DeleteConfirm -> {
                val id = state.value.deletingId ?: return
                updateState { copy(deletingId = null) }
                executeAsync {
                    deleteStadiumUseCase(id).collect {
                        updateState { copy(stadiums = stadiums.filter { it.id != id }) }
                        sendEffect(HomeContract.Effect.ShowToast("Stadion o'chirildi"))
                    }
                }
            }
            is HomeContract.Event.SelectTournament -> updateState { copy(selectedTournament = event.t) }
            HomeContract.Event.ClearTournament -> updateState { copy(selectedTournament = null) }
            is HomeContract.Event.SelectMatch -> updateState { copy(selectedMatch = event.m) }
            HomeContract.Event.ClearMatch -> updateState { copy(selectedMatch = null) }
        }
    }

    private fun loadStadiums(page: Int) {
        val s = state.value
        updateState { copy(isLoadingStadiums = true, stadiumError = null) }
        executeAsync(
            block = {
                var result = uz.coder.foottopbusiness.data.network.dto.stadium.PageStadiumResponseDto()
                getStadiumsUseCase(
                    name = s.searchQuery.takeIf { it.isNotBlank() },
                    isActive = s.filterActive,
                    page = page,
                ).collect { result = it }
                result
            },
            onSuccess = { pageData ->
                val newItems = pageData.content ?: emptyList()
                updateState {
                    copy(
                        stadiums = if (page == 0) newItems else stadiums + newItems,
                        currentPage = page,
                        isLastPage = pageData.last ?: true,
                        isLoadingStadiums = false,
                    )
                }
            },
            onError = { updateState { copy(isLoadingStadiums = false, stadiumError = it.message) } }
        )
    }

    private fun loadTournaments() {
        updateState { copy(isLoadingTournaments = true) }
        executeAsync(
            block = { var r = emptyList<TournamentResponseDto>(); getTournamentsUseCase().collect { r = it }; r },
            onSuccess = { updateState { copy(tournaments = it, isLoadingTournaments = false) } },
            onError = { updateState { copy(isLoadingTournaments = false) } }
        )
    }

    private fun loadMatches() {
        updateState { copy(isLoadingMatches = true) }
        executeAsync(
            block = { var r = emptyList<MatchResponseDto>(); getMatchesUseCase().collect { r = it }; r },
            onSuccess = { updateState { copy(matches = it, isLoadingMatches = false) } },
            onError = { updateState { copy(isLoadingMatches = false) } }
        )
    }
}
