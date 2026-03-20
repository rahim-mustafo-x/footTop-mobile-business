package uz.coder.foottopbusiness.presentation.main.coaches

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.usecase.coach.CreateCoachUseCase
import uz.coder.foottopbusiness.domain.usecase.coach.GetCoachesUseCase

class CoachesViewModel(
    private val getCoachesUseCase: GetCoachesUseCase,
    private val createCoachUseCase: CreateCoachUseCase,
) : BaseViewModel<CoachesContract.State, CoachesContract.Effect, CoachesContract.Event>(
    initialState = CoachesContract.State()
) {
    init { handleEvent(CoachesContract.Event.Load) }

    override fun handleEvent(event: CoachesContract.Event) {
        when (event) {
            CoachesContract.Event.Load -> {
                updateState { copy(isLoading = true, error = null) }
                executeAsync(
                    block = { getCoachesUseCase().let { flow -> var result = emptyList<CoachResponseDto>(); flow.collect { result = it }; result } },
                    onSuccess = { updateState { copy(coaches = it, isLoading = false) } },
                    onError = { updateState { copy(error = it.message, isLoading = false) } }
                )
            }
            is CoachesContract.Event.SelectCoach -> updateState { copy(selectedCoach = event.coach) }
            CoachesContract.Event.ClearDetail -> updateState { copy(selectedCoach = null) }
            CoachesContract.Event.ShowCreateDialog -> updateState { copy(showCreateDialog = true) }
            CoachesContract.Event.HideCreateDialog -> updateState { copy(showCreateDialog = false) }
            is CoachesContract.Event.Create -> {
                updateState { copy(isCreating = true, showCreateDialog = false) }
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
                        sendEffect(CoachesContract.Effect.ShowToast("Murabbiy qo'shildi"))
                    },
                    onError = {
                        updateState { copy(isCreating = false) }
                        sendEffect(CoachesContract.Effect.ShowToast(it.message ?: "Xatolik"))
                    }
                )
            }
        }
    }
}
