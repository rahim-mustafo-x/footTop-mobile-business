package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto

enum class StadiumType(val label: String) {
    FOOTBALL("Football"),
    TENNIS("Tennis"),
}

enum class StadiumDuration(val label: String) {
    SIXTY("60 min"),
    NINETY("90 min"),
    ONE_HUNDRED_TWENTY("120 min"),
}

sealed interface AddPitchContract {
    data class State(
        val name: String = "",
        val description: String = "",
        val type: StadiumType = StadiumType.FOOTBALL,
        val duration: StadiumDuration = StadiumDuration.SIXTY,
        val capacity: String = "",
        val pricePerHour: String = "",
        val openTime: String = "",
        val closeTime: String = "",
        val imageUrl: String = "",
        // region/district
        val regions: List<RegionDto> = emptyList(),
        val districts: List<DistrictDto> = emptyList(),
        val selectedRegion: RegionDto? = null,
        val selectedDistrict: DistrictDto? = null,
        val showRegionDropdown: Boolean = false,
        val showDistrictDropdown: Boolean = false,
        // dropdowns
        val showTypeDropdown: Boolean = false,
        val showDurationDropdown: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateBack : Effect
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        data class Name(val value: String) : Event
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
        data class ShowRegionDropdown(val show: Boolean) : Event
        data class ShowDistrictDropdown(val show: Boolean) : Event
        data class ShowTypeDropdown(val show: Boolean) : Event
        data class ShowDurationDropdown(val show: Boolean) : Event
        object Save : Event
    }
}
