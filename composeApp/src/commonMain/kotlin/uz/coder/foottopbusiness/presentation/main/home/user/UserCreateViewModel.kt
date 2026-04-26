package uz.coder.foottopbusiness.presentation.main.home.user

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.data.network.dto.UserRequestDto
import uz.coder.foottopbusiness.data.network.dto.admin.CreateStaffUserDto
import uz.coder.foottopbusiness.domain.repository.UserRepository
import uz.coder.foottopbusiness.domain.usecase.admin.CreateStaffUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase

class UserCreateViewModel(
    private val userRepository: UserRepository,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val getDistrictsUseCase: GetDistrictsUseCase,
    private val createStaffUseCase: CreateStaffUseCase
) : ScreenModel, UserCreateContract {

    private val _state = MutableStateFlow(UserCreateContract.State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UserCreateContract.Effect>()
    val effect = _effect.asSharedFlow()

    init {
        loadRegions()
    }

    private fun loadRegions() {
        getRegionsUseCase()
            .onStart { _state.update { it.copy(isLoadingRegions = true) } }
            .onEach { regions ->
                _state.update { it.copy(regions = regions, isLoadingRegions = false) }
            }
            .catch { t ->
                _state.update { it.copy(isLoadingRegions = false) }
                _effect.emit(UserCreateContract.Effect.ShowError(t.message ?: "Regionlarni yuklashda xatolik"))
            }
            .launchIn(screenModelScope)
    }

    private fun loadDistricts(regionId: Int) {
        getDistrictsUseCase(regionId)
            .onStart { _state.update { it.copy(isLoadingDistricts = true, districts = emptyList(), selectedDistrict = null) } }
            .onEach { districts ->
                _state.update { it.copy(districts = districts, isLoadingDistricts = false) }
            }
            .catch { t ->
                _state.update { it.copy(isLoadingDistricts = false) }
                _effect.emit(UserCreateContract.Effect.ShowError(t.message ?: "Tumanlarni yuklashda xatolik"))
            }
            .launchIn(screenModelScope)
    }

    fun onEvent(event: UserCreateContract.Event) {
        when (event) {
            is UserCreateContract.Event.FullNameChanged -> {
                _state.update { it.copy(fullName = event.name) }
            }
            is UserCreateContract.Event.LoginChanged -> {
                _state.update { it.copy(login = event.login) }
            }
            is UserCreateContract.Event.PhoneChanged -> {
                _state.update { it.copy(phone = event.phone) }
            }
            is UserCreateContract.Event.PasswordChanged -> {
                _state.update { it.copy(password = event.password) }
            }
            is UserCreateContract.Event.AssignedStadiumChanged -> {
                _state.update { it.copy(assignedStadium = event.stadium) }
            }
            is UserCreateContract.Event.RoleChanged -> {
                _state.update { it.copy(role = event.role) }
            }
            UserCreateContract.Event.CreateClicked -> createUser()
            UserCreateContract.Event.GeneratePasswordClicked -> generatePassword()
            UserCreateContract.Event.LoadRegions -> loadRegions()
            is UserCreateContract.Event.RegionSelected -> {
                _state.update { it.copy(selectedRegion = event.region) }
                loadDistricts(event.region.id ?: return)
            }
            is UserCreateContract.Event.DistrictSelected -> {
                _state.update { it.copy(selectedDistrict = event.district) }
            }
        }
    }

    private fun generatePassword() {
        userRepository.generatePassword()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { password ->
                _state.update { it.copy(isLoading = false, password = password) }
            }
            .catch { t ->
                _state.update { it.copy(isLoading = false) }
                _effect.emit(UserCreateContract.Effect.ShowError(t.message ?: "Parol yaratishda xatolik"))
            }
            .launchIn(screenModelScope)
    }

    private fun createUser() {
        val currentState = _state.value
        if (currentState.fullName.isBlank() || currentState.phone.isBlank() || currentState.login.isBlank()) {
            screenModelScope.launch {
                _effect.emit(UserCreateContract.Effect.ShowError("Iltimos, barcha maydonlarni to'ldiring"))
            }
            return
        }

        if (currentState.role == "ROLE_DISTRICT_ADMIN" && currentState.selectedDistrict == null) {
            screenModelScope.launch {
                _effect.emit(UserCreateContract.Effect.ShowError("Iltimos, tumanni tanlang"))
            }
            return
        }

        val mappedRole = currentState.role.removePrefix("ROLE_")

        val staffDto = CreateStaffUserDto(
            fullName = currentState.fullName,
            phone = currentState.phone,
            username = currentState.login,
            password = currentState.password.ifBlank { "password123" },
            role = mappedRole,
            districtId = currentState.selectedDistrict?.id?.toLong()
        )

        createStaffUseCase(staffDto)
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
                _effect.emit(UserCreateContract.Effect.NavigateBack)
            }
            .catch { t ->
                _state.update { it.copy(isLoading = false) }
                _effect.emit(UserCreateContract.Effect.ShowError(t.message ?: "Xatolik yuz berdi"))
            }
            .launchIn(screenModelScope)
    }
}
