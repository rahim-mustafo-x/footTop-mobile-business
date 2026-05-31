package uz.coder.foottopbusiness.presentation.main.tournaments

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.tournament.PageTournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentFilterDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.CreateTournamentUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.UpdateTournamentUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase

class TournamentsViewModel(
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val createTournamentUseCase: CreateTournamentUseCase,
    private val updateTournamentUseCase: UpdateTournamentUseCase,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val getDistrictsUseCase: GetDistrictsUseCase,
    private val preferencesManager: PreferencesManager,
) : BaseViewModel<TournamentsContract.State, TournamentsContract.Effect, TournamentsContract.Event>(
    initialState = TournamentsContract.State()
) {
    init { 
        handleEvent(TournamentsContract.Event.Load)
        loadRegions()
    }

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
            is TournamentsContract.Event.UpdateFilters -> {
                updateState { copy(filters = event.filters, page = 0, tournaments = emptyList(), isLastPage = false) }
                loadTournaments(0)
            }
            is TournamentsContract.Event.SelectRegion -> onRegionSelected(event.region)
            is TournamentsContract.Event.SelectDistrict -> updateState { copy(selectedDistrict = event.district, showDistrictDropdown = false) }
            is TournamentsContract.Event.ShowRegionDropdown -> updateState { copy(showRegionDropdown = event.show) }
            is TournamentsContract.Event.ShowDistrictDropdown -> updateState { copy(showDistrictDropdown = event.show) }
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
            is TournamentsContract.Event.Update -> {
                updateState { copy(isCreating = true) }
                executeAsync(
                    block = {
                        val userId = preferencesManager.userId.firstOrNull() ?: 0L
                        var result: TournamentResponseDto? = null
                        updateTournamentUseCase(
                            id = event.id,
                            request = TournamentRequestDto(
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
                    onSuccess = { updated ->
                        updateState {
                            copy(
                                isCreating = false,
                                tournaments = tournaments.map { if (it.id == updated.id) updated else it },
                                selectedTournament = if (selectedTournament?.id == updated.id) updated else selectedTournament
                            )
                        }
                        sendEffect(TournamentsContract.Effect.ShowToast("Turnir yangilandi"))
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
                var result: PageTournamentResponseDto? = null
                getTournamentsUseCase(page = page, filters = state.value.filters).collect { result = it }
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

    private fun loadRegions() {
        executeAsync(
            block = { getRegionsUseCase().first() },
            onSuccess = { updateState { copy(regions = it) } }
        )
    }

    private fun onRegionSelected(region: RegionDto) {
        updateState { copy(selectedRegion = region, selectedDistrict = null, districts = emptyList(), showRegionDropdown = false) }
        executeAsync(
            block = { getDistrictsUseCase(region.id).first() },
            onSuccess = { updateState { copy(districts = it) } }
        )
    }
}
