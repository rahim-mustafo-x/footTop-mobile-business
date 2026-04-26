package uz.coder.foottopbusiness.presentation.main.home.user

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto

interface UserCreateContract {
    data class State(
        val fullName: String = "",
        val login: String = "",
        val phone: String = "",
        val password: String = "",
        val assignedStadium: String = "",
        val role: String = "ROLE_OWNER",
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        
        val regions: List<RegionDto> = emptyList(),
        val selectedRegion: RegionDto? = null,
        val districts: List<DistrictDto> = emptyList(),
        val selectedDistrict: DistrictDto? = null,
        val isLoadingRegions: Boolean = false,
        val isLoadingDistricts: Boolean = false
    ) : MviState

    sealed interface Event : MviEvent {
        data class FullNameChanged(val name: String) : Event
        data class LoginChanged(val login: String) : Event
        data class PhoneChanged(val phone: String) : Event
        data class PasswordChanged(val password: String) : Event
        data class AssignedStadiumChanged(val stadium: String) : Event
        data class RoleChanged(val role: String) : Event
        data object CreateClicked : Event
        data object GeneratePasswordClicked : Event
        
        data object LoadRegions : Event
        data class RegionSelected(val region: RegionDto) : Event
        data class DistrictSelected(val district: DistrictDto) : Event
    }

    sealed interface Effect : MviEffect {
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
    }
}
