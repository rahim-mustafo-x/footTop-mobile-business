package uz.coder.foottopbusiness.presentation.main.stadium

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase

class StadiumViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
) : BaseViewModel<StadiumContract.State, StadiumContract.Effect, StadiumContract.Event>(
    initialState = StadiumContract.State()
) {
    init {
        handleEvent(StadiumContract.Event.Load)
    }

    override fun handleEvent(event: StadiumContract.Event) {
        when (event) {
            StadiumContract.Event.Load, StadiumContract.Event.Refresh -> {
                updateState { copy(currentPage = 0, stadiums = emptyList(), isLastPage = false) }
                loadStadiums(0)
            }
            is StadiumContract.Event.Search -> {
                updateState { copy(searchQuery = event.query, currentPage = 0, stadiums = emptyList()) }
                loadStadiums(0)
            }
            is StadiumContract.Event.FilterActive -> {
                updateState { copy(filterActive = event.isActive, currentPage = 0, stadiums = emptyList()) }
                loadStadiums(0)
            }
            StadiumContract.Event.LoadNextPage -> {
                val s = state.value
                if (!s.isLastPage && !s.isLoading) loadStadiums(s.currentPage + 1)
            }
            is StadiumContract.Event.StadiumClick -> {
                sendEffect(StadiumContract.Effect.NavigateToDetails(event.stadium))
            }
        }
    }

    private fun loadStadiums(page: Int) {
        val s = state.value
        updateState { copy(isLoading = true, error = null) }
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
                        isLoading = false,
                    )
                }
            },
            onError = { updateState { copy(isLoading = false, error = it.message) } }
        )
    }
}
