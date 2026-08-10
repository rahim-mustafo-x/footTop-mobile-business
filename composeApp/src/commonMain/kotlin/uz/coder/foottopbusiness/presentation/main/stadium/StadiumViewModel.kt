package uz.coder.foottopbusiness.presentation.main.stadium

import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.domain.usecase.stadium.DeleteStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase

class StadiumViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
    private val deleteStadiumUseCase: DeleteStadiumUseCase,
    private val userSession: UserSession,
) : BaseViewModel<StadiumContract.State, StadiumContract.Effect, StadiumContract.Event>(
    initialState = StadiumContract.State()
) {
    init {
        handleEvent(StadiumContract.Event.Load)
        observeRole()
    }

    /**
     * Rol yagona manbadan olinadi.
     *
     * Ilgari bu yerda `roles.any { it.name == "STADIUM_OWNER" || "OWNER" }`
     * tekshiruvi turardi, backend esa "ROLE_OWNER" qaytaradi - shuning uchun
     * stadion egasiga hech qachon `isOwner = true` bo'lmasdi va u "Jadval"
     * o'rniga admin ko'rinishini ("Stadionlar" + qo'shish tugmasi) ko'rardi.
     */
    private fun observeRole() {
        executeAsync {
            userSession.role.collect { role ->
                updateState { copy(userRole = role, isOwner = role == UserRole.OWNER) }
            }
        }
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
            is StadiumContract.Event.RequestDelete -> {
                updateState { copy(stadiumToDelete = event.stadium) }
            }
            StadiumContract.Event.DismissDelete -> {
                updateState { copy(stadiumToDelete = null) }
            }
            StadiumContract.Event.ConfirmDelete -> {
                val stadium = state.value.stadiumToDelete ?: return
                updateState { copy(stadiumToDelete = null, isLoading = true) }
                executeAsync {
                    deleteStadiumUseCase(stadium.id ?: return@executeAsync).collect {
                        sendEffect(StadiumContract.Effect.ShowToast("Stadion muvaffaqiyatli o'chirildi"))
                        handleEvent(StadiumContract.Event.Refresh)
                    }
                }
            }
        }
    }

    private fun loadStadiums(page: Int) {
        val s = state.value
        updateState { copy(isLoading = true, error = null, hasError = false) }
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
                        hasError = false,
                    )
                }
            },
            onError = { updateState { copy(isLoading = false, error = it.message, hasError = true) } }
        )
    }
}
