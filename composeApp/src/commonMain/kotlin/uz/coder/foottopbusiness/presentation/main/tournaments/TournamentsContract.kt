package uz.coder.foottopbusiness.presentation.main.tournaments

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentFilterDto

sealed interface TournamentsContract {
    data class State(
        val tournaments: List<TournamentResponseDto> = emptyList(),
        val isLoading: Boolean = false,
        val isMoreLoading: Boolean = false,
        val error: String? = null,
        val selectedTournament: TournamentResponseDto? = null,
        val isCreating: Boolean = false,
        val showCreateDialog: Boolean = false,
        val page: Int = 0,
        val isLastPage: Boolean = false,
        val filters: TournamentFilterDto = TournamentFilterDto(),
        
        // Address selection
        val regions: List<uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto> = emptyList(),
        val districts: List<uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto> = emptyList(),
        val selectedRegion: uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto? = null,
        val selectedDistrict: uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto? = null,
        val showRegionDropdown: Boolean = false,
        val showDistrictDropdown: Boolean = false,
        val latitude: Double? = null,
        val longitude: Double? = null
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object LoadMore : Event
        data class Select(val tournament: TournamentResponseDto) : Event
        object ClearDetail : Event
        object ShowCreateDialog : Event
        object HideCreateDialog : Event
        data class UpdateFilters(val filters: TournamentFilterDto) : Event
        
        // Address selection events
        data class SelectRegion(val region: uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto) : Event
        data class SelectDistrict(val district: uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto) : Event
        data class ShowRegionDropdown(val show: Boolean) : Event
        data class ShowDistrictDropdown(val show: Boolean) : Event
        data class Latitude(val value: Double?) : Event
        data class Longitude(val value: Double?) : Event
        object GetCurrentLocation : Event

        data class Create(
            val name: String,
            val startDate: String,
            val endDate: String,
            val maxTeams: Int,
            val entryFee: Double,
            val address: String?,
            val startTime: String?,
            val endTime: String?,
            val latitude: Double? = null,
            val longitude: Double? = null
        ) : Event

        data class Update(
            val id: Long,
            val name: String,
            val startDate: String,
            val endDate: String,
            val maxTeams: Int,
            val entryFee: Double,
            val address: String?,
            val startTime: String?,
            val endTime: String?,
            val latitude: Double? = null,
            val longitude: Double? = null
        ) : Event
    }
}
