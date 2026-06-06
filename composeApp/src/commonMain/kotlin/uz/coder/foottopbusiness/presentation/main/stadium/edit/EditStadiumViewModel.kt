package uz.coder.foottopbusiness.presentation.main.stadium.edit

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.formatAsTime
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.core.platform.getCurrentLocation
import uz.coder.foottopbusiness.core.platform.checkLocationPermissionStatus
import uz.coder.foottopbusiness.core.platform.PermissionStatus
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.domain.repository.UserRepository
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.UpdateStadiumUseCase
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.StadiumDuration
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.StadiumType

class EditStadiumViewModel(
    stadium: StadiumResponse,
    private val updateStadiumUseCase: UpdateStadiumUseCase,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val getDistrictsUseCase: GetDistrictsUseCase,
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager,
) : BaseViewModel<EditStadiumContract.State, EditStadiumContract.Effect, EditStadiumContract.Event>(
    initialState = EditStadiumContract.State(
        id = stadium.id ?: 0,
        name = stadium.name ?: "",
        phone = stadium.phone ?: "",
        description = stadium.description ?: "",
        capacity = stadium.capacity?.toString() ?: "",
        pricePerHour = stadium.pricePerHour?.toInt()?.toString() ?: "",
        openTime = if (stadium.openTime.isNullOrBlank()) "08:00" else try { LocalDateTime.parse(stadium.openTime).formatAsTime() } catch (_: Exception) { stadium.openTime },
        closeTime = if (stadium.closeTime.isNullOrBlank()) "22:00" else try { LocalDateTime.parse(stadium.closeTime).formatAsTime() } catch (_: Exception) { stadium.closeTime },
        // Note: images are handled differently in StadiumResponse vs CreateRequest
        // For simplicity assuming first image or empty
        imageUrl = "",
        latitude = stadium.location?.latitude,
        longitude = stadium.location?.longitude,
        preciseAddress = stadium.name ?: "", // Or some other field if available
        type = try { StadiumType.valueOf(stadium.type ?: "FOOTBALL") } catch (_: Exception) { StadiumType.FOOTBALL },
        duration = try { StadiumDuration.valueOf(stadium.duration ?: "SIXTY") } catch (_: Exception) { StadiumDuration.SIXTY }
    )
) {

    init {
        loadUserRole()
        loadRegions()
    }

    private fun loadUserRole() {
        executeAsync(
            block = {
                val roleStr = preferencesManager.role.firstOrNull()
                UserRole.fromString(roleStr)
            },
            onSuccess = { role ->
                updateState { copy(userRole = role) }
                if (role == UserRole.SUPER_ADMIN || role == UserRole.DISTRICT_ADMIN) {
                    loadOwners()
                }
            }
        )
    }

    private fun loadOwners() {
        executeAsync(
            block = { userRepository.getAllUsers().first() },
            onSuccess = { users ->
                updateState { copy(owners = users) }
            }
        )
    }

    override fun handleEvent(event: EditStadiumContract.Event) {
        when (event) {
            is EditStadiumContract.Event.Name -> updateState { copy(name = event.value) }
            is EditStadiumContract.Event.Phone -> updateState { copy(phone = event.value) }
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
            is EditStadiumContract.Event.SelectOwner -> updateState { copy(selectedOwner = event.owner, showOwnerDropdown = false) }
            is EditStadiumContract.Event.ShowRegionDropdown -> updateState { copy(showRegionDropdown = event.show) }
            is EditStadiumContract.Event.ShowDistrictDropdown -> updateState { copy(showDistrictDropdown = event.show) }
            is EditStadiumContract.Event.ShowOwnerDropdown -> updateState { copy(showOwnerDropdown = event.show) }
            is EditStadiumContract.Event.ShowTypeDropdown -> updateState { copy(showTypeDropdown = event.show) }
            is EditStadiumContract.Event.ShowDurationDropdown -> updateState { copy(showDurationDropdown = event.show) }
            is EditStadiumContract.Event.Latitude -> updateState { copy(latitude = event.value) }
            is EditStadiumContract.Event.Longitude -> updateState { copy(longitude = event.value) }
            is EditStadiumContract.Event.PreciseAddress -> updateState { copy(preciseAddress = event.value) }
            EditStadiumContract.Event.GetCurrentLocation -> handleLocationRequest()
            is EditStadiumContract.Event.OnLocationPermissionResult -> {
                updateState { copy(triggerLocationPermission = false) }
                if (event.status == PermissionStatus.GRANTED) {
                    fetchCurrentLocation()
                } else {
                    sendEffect(EditStadiumContract.Effect.ShowToast("Joylashuv ruxsati berilmadi"))
                }
            }
            is EditStadiumContract.Event.TriggerLocationPermission -> updateState { copy(triggerLocationPermission = event.trigger) }
            is EditStadiumContract.Event.Save -> save()
        }
    }

    private fun handleLocationRequest() {
        executeAsync(
            block = { checkLocationPermissionStatus() },
            onSuccess = { status ->
                if (status == PermissionStatus.GRANTED) {
                    fetchCurrentLocation()
                } else {
                    updateState { copy(triggerLocationPermission = true) }
                }
            }
        )
    }

    private fun fetchCurrentLocation() {
        executeAsync(
            block = { getCurrentLocation() },
            onSuccess = { location ->
                location?.let {
                    updateState { copy(latitude = it.first, longitude = it.second) }
                } ?: sendEffect(EditStadiumContract.Effect.ShowToast("Joylashuvni aniqlab bo'lmadi"))
            }
        )
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
                    description = s.description.ifBlank { "Tavsif berilmagan" },
                    type = s.type.name,
                    duration = s.duration.name,
                    capacity = s.capacity.toIntOrNull() ?: 0,
                    pricePerHour = s.pricePerHour.toIntOrNull() ?: 0,
                    openTime = s.openTime,
                    closeTime = s.closeTime,
                    imageUrl = s.imageUrl,
                    regionId = s.selectedRegion?.id ?: 0,
                    districtId = s.selectedDistrict?.id ?: 0,
                    ownerId = s.selectedOwner?.id?.toInt(),
                    phone = s.phone,
                    latitude = s.latitude,
                    longitude = s.longitude,
                    address = s.preciseAddress
                ).first()
            },
            onSuccess = {
                updateState { copy(isLoading = false) }
                sendEffect(EditStadiumContract.Effect.NavigateBack)
            }
        )
    }
}
