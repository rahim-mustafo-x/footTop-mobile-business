package uz.coder.foottopbusiness.presentation.main.coaches

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto

sealed interface CoachesContract {
    data class State(
        val coaches: List<CoachResponseDto> = emptyList(),
        val filteredCoaches: List<CoachResponseDto> = emptyList(),
        val searchQuery: String = "",
        val selectedRoleFilter: Int = 0, // 0: All, 1: Admin, 2: Owner, 3: Coach
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedCoach: CoachResponseDto? = null,
        val isCreating: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        data class SelectCoach(val coach: CoachResponseDto) : Event
        object ClearDetail : Event
        data class Create(
            val userId: Long,
            val specialty: String,
            val experienceYears: Int,
            val hourlyRate: Double,
            val availability: String?,
        ) : Event
        data class Search(val query: String) : Event
        data class FilterByRole(val roleIndex: Int) : Event
    }
}
