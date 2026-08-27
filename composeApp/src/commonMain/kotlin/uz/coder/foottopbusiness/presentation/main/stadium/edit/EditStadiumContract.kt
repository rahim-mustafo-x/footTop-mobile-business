package uz.coder.foottopbusiness.presentation.main.stadium.edit

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.ImageDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.StadiumDuration
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.StadiumType

sealed interface EditStadiumContract {
    data class State(
        val id: Int = 0,
        val name: String = "",
        val phone: String = "",
        val description: String = "",
        val type: StadiumType = StadiumType.FOOTBALL,
        val duration: StadiumDuration = StadiumDuration.SIXTY,
        val capacity: String = "",
        val pricePerHour: String = "",
        val openTime: String = "",
        val closeTime: String = "",
        val imageUrl: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null,
        val preciseAddress: String = "",
        // region/district
        val regions: List<RegionDto> = emptyList(),
        val districts: List<DistrictDto> = emptyList(),
        val selectedRegion: RegionDto? = null,
        val selectedDistrict: DistrictDto? = null,
        /** Stadionning hozirgi hududi -- foydalanuvchi qayta tanlamasa shu saqlanadi. */
        val originalRegionId: Int? = null,
        val originalDistrictId: Int? = null,
        /** Mavjud rasmlar. Saqlashda qayta uzatiladi, aks holda serverda o'chib ketadi. */
        val existingImages: List<ImageDto> = emptyList(),
        val showRegionDropdown: Boolean = false,
        val showDistrictDropdown: Boolean = false,
        // owners
        val owners: List<UserDto> = emptyList(),
        val selectedOwner: UserDto? = null,
        val showOwnerDropdown: Boolean = false,
        val userRole: UserRole = UserRole.UNKNOWN,
        // dropdowns
        val showTypeDropdown: Boolean = false,
        val showDurationDropdown: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val triggerLocationPermission: Boolean = false
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateBack : Effect
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        data class Name(val value: String) : Event
        data class Phone(val value: String) : Event
        data class Description(val value: String) : Event
        data class Type(val value: StadiumType) : Event
        data class Duration(val value: StadiumDuration) : Event
        data class Capacity(val value: String) : Event
        data class PricePerHour(val value: String) : Event
        data class OpenTime(val value: String) : Event
        data class CloseTime(val value: String) : Event
        data class ImageUrl(val value: String) : Event
        data class SelectRegion(val region: RegionDto) : Event
        data class SelectDistrict(val district: DistrictDto) : Event
        data class SelectOwner(val owner: UserDto) : Event
        data class ShowRegionDropdown(val show: Boolean) : Event
        data class ShowDistrictDropdown(val show: Boolean) : Event
        data class ShowOwnerDropdown(val show: Boolean) : Event
        data class ShowTypeDropdown(val show: Boolean) : Event
        data class ShowDurationDropdown(val show: Boolean) : Event
        data class Latitude(val value: Double?) : Event
        data class Longitude(val value: Double?) : Event
        data class PreciseAddress(val value: String) : Event
        object GetCurrentLocation : Event
        data class OnLocationPermissionResult(val status: uz.coder.foottopbusiness.core.platform.PermissionStatus) : Event
        data class TriggerLocationPermission(val trigger: Boolean) : Event
        object Save : Event
    }
}
