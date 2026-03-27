package uz.coder.foottopbusiness.presentation.main.home

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.zip
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.domain.usecase.auth.LogoutUseCase
import uz.coder.foottopbusiness.domain.usecase.match.GetMatchesUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.DeleteStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumByIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.UpdateOpenCloseTimeUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase
import uz.coder.foottopbusiness.data.local.PreferencesManager
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.presentation.main.home.HomeContract.Effect.*
import kotlin.time.Clock

class HomeViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
    private val deleteStadiumUseCase: DeleteStadiumUseCase,
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val getMatchesUseCase: GetMatchesUseCase,
    private val updateOpenCloseTimeUseCase: UpdateOpenCloseTimeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getStadiumByIdUseCase: GetStadiumByIdUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val preferencesManager: PreferencesManager,
) : BaseViewModel<HomeContract.State, HomeContract.Effect, HomeContract.Event>(
    initialState = HomeContract.State(
        selectedDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    )
) {
    init {
        handleEvent(HomeContract.Event.Load)
    }

    override fun handleEvent(event: HomeContract.Event) {
        when (event) {
            HomeContract.Event.Load, HomeContract.Event.Refresh -> {
                updateState { copy(currentPage = 0, stadiums = emptyList(), isLastPage = false) }
                loadUser()
                loadStadiums(0)
                loadTournaments()
                loadMatches()
                loadDashboardStats()
            }

            is HomeContract.Event.ChangeTab -> updateState { copy(currentTab = event.index) }
            
            is HomeContract.Event.SetShowStadiumTable -> updateState { copy(showStadiumTable = event.show) }

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
                        sendEffect(ShowToast("Stadion o'chirildi"))
                        loadDashboardStats()
                    }
                }
            }

            is HomeContract.Event.SelectTournament -> updateState { copy(selectedTournament = event.t) }
            HomeContract.Event.ClearTournament -> updateState { copy(selectedTournament = null) }
            is HomeContract.Event.SelectMatch -> updateState { copy(selectedMatch = event.m) }
            HomeContract.Event.ClearMatch -> updateState { copy(selectedMatch = null) }

            is HomeContract.Event.SelectStadiumForSlots -> {
                updateState { 
                    copy(
                        selectedStadiumForTime = event.stadium,
                        newOpenTime = event.stadium.openTime ?: "",
                        newCloseTime = event.stadium.closeTime ?: ""
                    ) 
                }
                loadSlots(event.stadium.id ?: return, state.value.selectedDate)
            }

            is HomeContract.Event.ChangeDate -> {
                updateState { copy(selectedDate = event.date) }
                state.value.selectedStadiumForTime?.id?.let { loadSlots(it, event.date) }
            }

            HomeContract.Event.ClearStadiumForSlots -> updateState { copy(selectedStadiumForTime = null, stadiumSlots = emptyList()) }

            is HomeContract.Event.UpdateTime -> {
                val stadiumId = state.value.selectedStadiumForTime?.id ?: return
                updateState { copy(isUpdatingTime = true) }
                executeAsync {
                    updateOpenCloseTimeUseCase(stadiumId, event.open, event.close).collect {
                        updateState {
                            copy(
                                isUpdatingTime = false,
                                selectedStadiumForTime = null,
                                stadiums = stadiums.map {
                                    if (it.id == stadiumId) it.copy(
                                        openTime = event.open,
                                        closeTime = event.close
                                    ) else it
                                }
                            )
                        }
                        sendEffect(ShowToast("Vaqt yangilandi"))
                    }
                }
            }

            HomeContract.Event.Logout -> {
                executeAsync {
                    logoutUseCase()
                }
            }

            HomeContract.Event.Match -> sendEffect(Match)
            HomeContract.Event.Stadium -> sendEffect(Stadium)
            HomeContract.Event.Tournament -> sendEffect(Tournament)
        }
    }

    private fun loadUser() {
        executeAsync {
            val userId = preferencesManager.userId.firstOrNull() ?: return@executeAsync
            getUserUseCase(userId.toLong()).collect { result ->
                updateState { copy(user = result) }
            }
        }
    }

    private fun loadSlots(id: Int, date: String) {
        updateState { copy(isLoadingSlots = true) }
        executeAsync {
            getStadiumByIdUseCase(id, date, "SIXTY").collect { responseList ->
                val stadium = responseList.firstOrNull()
                updateState { copy(stadiumSlots = stadium?.slots ?: emptyList(), isLoadingSlots = false) }
            }
        }
    }

    private fun loadDashboardStats() {
        executeAsync {
            getStadiumsUseCase(isActive = null).zip(getTournamentsUseCase()) { stadiumsPage, tournaments ->
                updateState {
                    copy(
                        totalEarnings = (stadiumsPage.content?.sumOf { it.pricePerHour ?: 0.0 } ?: 0.0) * 0.8,
                        activeStadiums = stadiumsPage.content?.count { it.isActive == true } ?: 0,
                        totalTournaments = tournaments.size
                    )
                }
            }.zip(getMatchesUseCase()) { _, matches ->
                updateState { copy(totalMatches = matches.size) }
            }.collect {}
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
            block = {
                var r = emptyList<TournamentResponseDto>()
                getTournamentsUseCase().collect { r = it }
                r
            },
            onSuccess = { updateState { copy(tournaments = it, isLoadingTournaments = false) } },
            onError = { updateState { copy(isLoadingTournaments = false) } }
        )
    }

    private fun loadMatches() {
        updateState { copy(isLoadingMatches = true) }
        executeAsync(
            block = {
                var r = emptyList<MatchResponseDto>()
                getMatchesUseCase().collect { r = it }
                r
            },
            onSuccess = { updateState { copy(matches = it, isLoadingMatches = false) } },
            onError = { updateState { copy(isLoadingMatches = false) } }
        )
    }
}
