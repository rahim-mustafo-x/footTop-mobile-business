package uz.coder.foottopbusiness.presentation.main.coaches

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.usecase.coach.CreateCoachUseCase
import uz.coder.foottopbusiness.domain.usecase.coach.GetCoachesUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetAllUsersUseCase

class CoachesViewModel(
    private val getCoachesUseCase: GetCoachesUseCase,
    private val createCoachUseCase: CreateCoachUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
) : BaseViewModel<CoachesContract.State, CoachesContract.Effect, CoachesContract.Event>(
    initialState = CoachesContract.State()
) {
    init { 
        handleEvent(CoachesContract.Event.Load)
        loadUsers()
    }

    private fun loadUsers() {
        executeAsync(
            block = { 
                var result = emptyList<uz.coder.foottopbusiness.data.network.dto.UserDto>()
                getAllUsersUseCase().collect { result = it }
                result
            },
            onSuccess = { updateState { copy(users = it) } }
        )
    }

    override fun handleEvent(event: CoachesContract.Event) {
        when (event) {
            CoachesContract.Event.Load -> {
                updateState { copy(isLoading = true, error = null) }
                executeAsync(
                    block = { getCoachesUseCase().let { flow -> var result = emptyList<CoachResponseDto>(); flow.collect { result = it }; result } },
                    onSuccess = { updateState { copy(coaches = it, filteredCoaches = it, isLoading = false) } },
                    onError = { updateState { copy(error = it.message, isLoading = false) } }
                )
            }
            is CoachesContract.Event.SelectCoach -> updateState { copy(selectedCoach = event.coach) }
            CoachesContract.Event.ClearDetail -> updateState { copy(selectedCoach = null) }
            is CoachesContract.Event.Create -> {
                updateState { copy(isCreating = true) }
                executeAsync(
                    block = {
                        var result: CoachResponseDto? = null
                        createCoachUseCase(
                            CoachRequestDto(
                                userId = event.userId,
                                specialty = event.specialty,
                                experienceYears = event.experienceYears,
                                hourlyRate = event.hourlyRate,
                                availability = event.availability,
                            )
                        ).collect { result = it }
                        result!!
                    },
                    onSuccess = { created ->
                        updateState { copy(isCreating = false, coaches = coaches + created) }
                        applyFilter()
                        sendEffect(CoachesContract.Effect.ShowToast("Murabbiy qo'shildi"))
                    },
                    onError = {
                        updateState { copy(isCreating = false) }
                        sendEffect(CoachesContract.Effect.ShowToast(it.message ?: "Xatolik"))
                    }
                )
            }
            is CoachesContract.Event.Search -> {
                updateState { copy(searchQuery = event.query) }
                applyFilter()
            }
            is CoachesContract.Event.FilterByRole -> {
                updateState { copy(selectedRoleFilter = event.roleIndex) }
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val state = state.value
        val query = state.searchQuery.lowercase()
        val roleIndex = state.selectedRoleFilter

        val filtered = state.coaches.filter { coach ->
            val matchesQuery = query.isEmpty() ||
                coach.coachName?.lowercase()?.contains(query) == true ||
                coach.specialty?.lowercase()?.contains(query) == true

            val matchesRole = when (roleIndex) {
                0 -> true // All
                1 -> coach.specialty == "ADMIN"
                2 -> coach.specialty == "EGASI" || coach.specialty == "OWNER"
                3 -> coach.specialty == "MURABBIY" || coach.specialty == "COACH"
                else -> true
            }

            matchesQuery && matchesRole
        }

        updateState { copy(filteredCoaches = filtered) }
    }
}
