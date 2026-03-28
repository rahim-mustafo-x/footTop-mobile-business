package uz.coder.foottopbusiness.presentation.main.stadium.edit

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.UpdateStadiumUseCase
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.StadiumDuration
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.StadiumType

class EditStadiumViewModel(
    stadium: StadiumResponse,
    private val updateStadiumUseCase: UpdateStadiumUseCase,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val getDistrictsUseCase: GetDistrictsUseCase,
) : BaseViewModel<EditStadiumContract.State, EditStadiumContract.Effect, EditStadiumContract.Event>(
    initialState = EditStadiumContract.State(
        id = stadium.id ?: 0,
        name = stadium.name ?: "",
        description = stadium.description ?: "",
        capacity = stadium.capacity?.toString() ?: "",
        pricePerHour = stadium.pricePerHour?.toInt()?.toString() ?: "",
        openTime = LocalDateTime.parse(stadium.openTime?:"").formatAsTime(),
        closeTime = LocalDateTime.parse(stadium.closeTime?:"").formatAsTime(),
        // Note: images are handled differently in StadiumResponse vs CreateRequest
        // For simplicity assuming first image or empty
        imageUrl = "",
        type = try { StadiumType.valueOf(stadium.type ?: "FOOTBALL") } catch (_: Exception) { StadiumType.FOOTBALL },
        duration = try { StadiumDuration.valueOf(stadium.duration ?: "SIXTY") } catch (_: Exception) { StadiumDuration.SIXTY }
    )
) {

    init {
        loadRegions()
    }

    override fun handleEvent(event: EditStadiumContract.Event) {
        when (event) {
            is EditStadiumContract.Event.Name -> updateState { copy(name = event.value) }
            is EditStadiumContract.Event.Description -> updateState { copy(description = event.value) }
            is EditStadiumContract.Event.Type -> updateState { copy(type = event.value, showTypeDropdown = false) }
            is EditStadiumContract.Event.Duration -> updateState { copy(duration = event.value, showDurationDropdown = false) }
            is EditStadiumContract.Event.Capacity -> updateState { copy(capacity = event.value) }
            is EditStadiumContract.Event.PricePerHour -> updateState { copy(pricePerHour = event.value) }
            is EditStadiumContract.Event.OpenTime -> updateState { copy(openTime = event.value) }
            is EditStadiumContract.Event.CloseTime -> updateState { copy(closeTime = event.value) }
            is EditStadiumContract.Event.ImageUrl -> updateState { copy(imageUrl = event.value) }
            is EditStadiumContract.Event.SelectRegion -> onRegionSelected(event.region)
            is EditStadiumContract.Event.SelectDistrict -> onDistrictSelected(event.district)
            is EditStadiumContract.Event.ShowRegionDropdown -> updateState { copy(showRegionDropdown = event.show) }
            is EditStadiumContract.Event.ShowDistrictDropdown -> updateState { copy(showDistrictDropdown = event.show) }
            is EditStadiumContract.Event.ShowTypeDropdown -> updateState { copy(showTypeDropdown = event.show) }
            is EditStadiumContract.Event.ShowDurationDropdown -> updateState { copy(showDurationDropdown = event.show) }
            is EditStadiumContract.Event.Save -> save()
        }
    }

    private fun loadRegions() {
        executeAsync(
            block = { getRegionsUseCase().first() },
            onSuccess = { regions ->
                updateState { copy(regions = regions) }
                // If stadium has region, we might want to pre-select it and load districts
                // But stadium response names are strings, IDs are needed for selection logic
            }
        )
    }

    private fun onRegionSelected(region: RegionDto) {
        updateState { copy(selectedRegion = region, selectedDistrict = null, districts = emptyList(), showRegionDropdown = false) }
        executeAsync(
            block = { getDistrictsUseCase(region.id).first() },
            onSuccess = { districts -> updateState { copy(districts = districts) } }
        )
    }

    private fun onDistrictSelected(district: DistrictDto) {
        updateState { copy(selectedDistrict = district, showDistrictDropdown = false) }
    }

    private fun save() {
        val s = state.value
        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            onError = { e ->
                updateState { copy(isLoading = false) }
                sendEffect(EditStadiumContract.Effect.ShowToast(e.message ?: "Xatolik yuz berdi"))
            },
            block = {
                updateStadiumUseCase(
                    id = s.id,
                    name = s.name,
                    description = s.description,
                    type = s.type.name,
                    duration = s.duration.name,
                    capacity = s.capacity.toIntOrNull() ?: 0,
                    pricePerHour = s.pricePerHour.toIntOrNull() ?: 0,
                    openTime = s.openTime,
                    closeTime = s.closeTime,
                    imageUrl = s.imageUrl,
                    regionId = s.selectedRegion?.id ?: 0,
                    districtId = s.selectedDistrict?.id ?: 0,
                ).first()
            },
            onSuccess = {
                updateState { copy(isLoading = false) }
                sendEffect(EditStadiumContract.Effect.NavigateBack)
            }
        )
    }
}
