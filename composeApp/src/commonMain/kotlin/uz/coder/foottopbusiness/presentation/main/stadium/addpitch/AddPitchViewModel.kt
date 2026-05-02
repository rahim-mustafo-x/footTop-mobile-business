package uz.coder.foottopbusiness.presentation.main.stadium.addpitch

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.domain.repository.UserRepository
import uz.coder.foottopbusiness.domain.usecase.stadium.CreateStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.user.UserIdUseCase

class AddPitchViewModel(
    private val createStadiumUseCase: CreateStadiumUseCase,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val getDistrictsUseCase: GetDistrictsUseCase,
    private val saveRegionIdUseCase: SaveRegionIdUseCase,
    private val saveDistrictIdUseCase: SaveDistrictIdUseCase,
    private val getSavedRegionIdUseCase: GetSavedRegionIdUseCase,
    private val getSavedDistrictIdUseCase: GetSavedDistrictIdUseCase,
    private val userIdUseCase: UserIdUseCase,
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager,
) : BaseViewModel<AddPitchContract.State, AddPitchContract.Effect, AddPitchContract.Event>(
    initialState = AddPitchContract.State()
) {
    private val logLabel = "AddPitchVM"

    init {
        log(logLabel, "ViewModel initialized, loading regions...")
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
                // Filter users who are owners or just show all for now
                updateState { copy(owners = users) }
            }
        )
    }

    override fun handleEvent(event: AddPitchContract.Event) {
        when (event) {
            is AddPitchContract.Event.Name -> updateState { copy(name = event.value) }
            is AddPitchContract.Event.Phone -> updateState { copy(phone = event.value) }
            is AddPitchContract.Event.Description -> updateState { copy(description = event.value) }
            is AddPitchContract.Event.Type -> updateState { copy(type = event.value, showTypeDropdown = false) }
            is AddPitchContract.Event.Duration -> updateState { copy(duration = event.value, showDurationDropdown = false) }
            is AddPitchContract.Event.Capacity -> updateState { copy(capacity = event.value) }
            is AddPitchContract.Event.PricePerHour -> updateState { copy(pricePerHour = event.value) }
            is AddPitchContract.Event.OpenTime -> updateState { copy(openTime = event.value) }
            is AddPitchContract.Event.CloseTime -> updateState { copy(closeTime = event.value) }
            is AddPitchContract.Event.ImageUrl -> updateState { copy(imageUrl = event.value) }
            is AddPitchContract.Event.SelectRegion -> onRegionSelected(event.region)
            is AddPitchContract.Event.SelectDistrict -> onDistrictSelected(event.district)
            is AddPitchContract.Event.SelectOwner -> updateState { copy(selectedOwner = event.owner, showOwnerDropdown = false) }
            is AddPitchContract.Event.ShowRegionDropdown -> updateState { copy(showRegionDropdown = event.show) }
            is AddPitchContract.Event.ShowDistrictDropdown -> updateState { copy(showDistrictDropdown = event.show) }
            is AddPitchContract.Event.ShowOwnerDropdown -> updateState { copy(showOwnerDropdown = event.show) }
            is AddPitchContract.Event.ShowTypeDropdown -> updateState { copy(showTypeDropdown = event.show) }
            is AddPitchContract.Event.ShowDurationDropdown -> updateState { copy(showDurationDropdown = event.show) }
            is AddPitchContract.Event.Save -> save()
        }
    }

    private fun loadRegions() {
        executeAsync(
            onLoading = { log(logLabel, "Fetching regions start") },
            onError = { 
                log(logLabel, "Fetching regions error: ${it.message}")
                sendEffect(AddPitchContract.Effect.ShowToast(it.message ?: "Regionlarni yuklab bo'lmadi")) 
            },
            block = { 
                log(logLabel, "Executing GetRegionsUseCase")
                getRegionsUseCase().first() 
            },
            onSuccess = { regions ->
                log(logLabel, "Regions loaded: ${regions.size} items")
                updateState { copy(regions = regions) }
            }
        )
    }

    private fun onRegionSelected(region: RegionDto) {
        log(logLabel, "Region selected: ${region.name} (ID: ${region.id})")
        updateState { copy(selectedRegion = region, selectedDistrict = null, districts = emptyList(), showRegionDropdown = false) }
        viewModelScope.launch {
            saveRegionIdUseCase(region.id)
        }
        executeAsync(
            onLoading = { log(logLabel, "Fetching districts start for region ${region.id}") },
            onError = { 
                log(logLabel, "Fetching districts error: ${it.message}")
                sendEffect(AddPitchContract.Effect.ShowToast(it.message ?: "Tumanlarni yuklab bo'lmadi")) 
            },
            block = { getDistrictsUseCase(region.id).first() },
            onSuccess = { districts -> 
                log(logLabel, "Districts loaded: ${districts.size} items")
                updateState { copy(districts = districts) } 
            }
        )
    }

    private fun onDistrictSelected(district: DistrictDto) {
        log(logLabel, "District selected: ${district.name} (ID: ${district.id})")
        updateState { copy(selectedDistrict = district, showDistrictDropdown = false) }
        viewModelScope.launch {
            saveDistrictIdUseCase(district.id?:0)
        }
    }

    private fun save() {
        val s = state.value

        // Validation
        if (s.name.isBlank()) {
            sendEffect(AddPitchContract.Effect.ShowToast("Stadion nomini kiriting"))
            return
        }
        if (s.description.isBlank()) {
            sendEffect(AddPitchContract.Effect.ShowToast("Tavsifni kiriting"))
            return
        }
        if (s.capacity.isBlank()) {
            sendEffect(AddPitchContract.Effect.ShowToast("Sig'imni kiriting"))
            return
        }
        if (s.pricePerHour.isBlank()) {
            sendEffect(AddPitchContract.Effect.ShowToast("Soatlik narxni kiriting"))
            return
        }
        if (s.openTime.isBlank()) {
            sendEffect(AddPitchContract.Effect.ShowToast("Ochilish vaqtini kiriting"))
            return
        }
        if (s.closeTime.isBlank()) {
            sendEffect(AddPitchContract.Effect.ShowToast("Yopilish vaqtini kiriting"))
            return
        }
        if (s.selectedRegion == null) {
            sendEffect(AddPitchContract.Effect.ShowToast("Viloyatni tanlang"))
            return
        }
        if (s.selectedDistrict == null) {
            sendEffect(AddPitchContract.Effect.ShowToast("Tumanni tanlang"))
            return
        }

        log(logLabel, "Saving stadium: ${s.name}")
        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            onError = { e ->
                log(logLabel, "Save error: ${e.message}")
                updateState { copy(isLoading = false) }
                sendEffect(AddPitchContract.Effect.ShowToast(e.message ?: "Xatolik yuz berdi"))
            },
            block = {
                val regionId = getSavedRegionIdUseCase().first()
                val districtId = getSavedDistrictIdUseCase().first()
                val userId = userIdUseCase()
                log(logLabel, "Using IDs: region=$regionId, district=$districtId, userId=$userId")
                
                createStadiumUseCase(
                    name = s.name,
                    description = s.description,
                    type = s.type.name,
                    duration = s.duration.name,
                    capacity = s.capacity.toIntOrNull() ?: 0,
                    pricePerHour = s.pricePerHour.toIntOrNull() ?: 0,
                    openTime = s.openTime,
                    closeTime = s.closeTime,
                    imageUrl = s.imageUrl,
                    regionId = regionId,
                    districtId = districtId,
                    ownerId = s.selectedOwner?.id?.toInt(),
                    phone = s.phone
                ).first()
            },
            onSuccess = {
                log(logLabel, "Save success")
                updateState { copy(isLoading = false) }
                sendEffect(AddPitchContract.Effect.NavigateBack)
            }
        )
    }
}
