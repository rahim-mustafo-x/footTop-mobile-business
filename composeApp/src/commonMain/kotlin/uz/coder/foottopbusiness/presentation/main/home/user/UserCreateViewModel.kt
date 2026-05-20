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
import kotlinx.coroutines.Job
import uz.coder.foottopbusiness.data.network.dto.UserRequestDto
import uz.coder.foottopbusiness.data.network.dto.admin.CreateStaffUserDto
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.repository.UserRepository
import uz.coder.foottopbusiness.domain.usecase.admin.CreateStaffUseCase
import uz.coder.foottopbusiness.domain.usecase.coach.CreateCoachUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetAllUsersUseCase

class UserCreateViewModel(
    private val userRepository: UserRepository,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val getDistrictsUseCase: GetDistrictsUseCase,
    private val createStaffUseCase: CreateStaffUseCase,
    private val createCoachUseCase: CreateCoachUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
) : ScreenModel, UserCreateContract {

    private val _state = MutableStateFlow(UserCreateContract.State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UserCreateContract.Effect>()
    val effect = _effect.asSharedFlow()

    private var createJob: Job? = null

    init {
        // Data loading moved to Event.LoadInitialData for better screen transition performance
    }

    private fun loadUsers() {
        getAllUsersUseCase()
            .onEach { users -> _state.update { it.copy(users = users) } }
            .launchIn(screenModelScope)
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
            UserCreateContract.Event.LoadInitialData -> {
                loadRegions()
                loadUsers()
            }
            UserCreateContract.Event.LoadRegions -> loadRegions()
            is UserCreateContract.Event.RegionSelected -> {
                _state.update { it.copy(selectedRegion = event.region) }
                loadDistricts(event.region.id ?: return)
            }
            is UserCreateContract.Event.DistrictSelected -> {
                _state.update { it.copy(selectedDistrict = event.district) }
            }
            is UserCreateContract.Event.SpecialtyChanged -> {
                _state.update { it.copy(specialty = event.value) }
            }
            is UserCreateContract.Event.ExperienceChanged -> {
                _state.update { it.copy(experience = event.value) }
            }
            is UserCreateContract.Event.HourlyRateChanged -> {
                _state.update { it.copy(hourlyRate = event.value) }
            }
            is UserCreateContract.Event.AvailabilityChanged -> {
                _state.update { it.copy(availability = event.value) }
            }
            is UserCreateContract.Event.UserSelected -> {
                _state.update { 
                    it.copy(
                        selectedUser = event.user,
                        fullName = event.user?.fullName ?: it.fullName,
                        login = event.user?.username ?: it.login,
                        phone = event.user?.phone ?: it.phone,
                        showUserDropdown = false
                    ) 
                }
            }
            is UserCreateContract.Event.ShowUserDropdown -> {
                _state.update { it.copy(showUserDropdown = event.show) }
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
        
        if (createJob?.isActive == true) return
        
        // If an existing user is selected, just call createCoach for ROLE_COACH
        if (currentState.role == "ROLE_COACH" && currentState.selectedUser != null) {
            promoteToCoach(currentState.selectedUser.id ?: 0L)
            return
        }

        if (currentState.fullName.isBlank() || currentState.phone.isBlank() || currentState.login.isBlank()) {
            screenModelScope.launch {
                _effect.emit(UserCreateContract.Effect.ShowError("Iltimos, barcha maydonlarni to'ldiring"))
            }
            return
        }

        if ((currentState.role == "ROLE_DISTRICT_ADMIN" || currentState.role == "ROLE_OWNER") && currentState.selectedDistrict == null) {
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

        createJob = createStaffUseCase(staffDto)
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { userDto ->
                if (currentState.role == "ROLE_COACH") {
                    promoteToCoach(userDto.id ?: 0L)
                } else {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                    _effect.emit(UserCreateContract.Effect.NavigateBack)
                }
            }
            .catch { t ->
                _state.update { it.copy(isLoading = false) }
                _effect.emit(UserCreateContract.Effect.ShowError(t.message ?: "Xatolik yuz berdi"))
            }
            .launchIn(screenModelScope)
    }

    private fun promoteToCoach(userId: Long) {
        if (createJob?.isActive == true && _state.value.isLoading) {
            // Already processing something
        }

        val currentState = _state.value
        val coachDto = CoachRequestDto(
            userId = userId,
            specialty = currentState.specialty,
            experienceYears = currentState.experience.toIntOrNull() ?: 0,
            hourlyRate = currentState.hourlyRate.toDoubleOrNull() ?: 0.0,
            availability = currentState.availability
        )
        createJob = createCoachUseCase(coachDto)
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
                _effect.emit(UserCreateContract.Effect.NavigateBack)
            }
            .catch { t ->
                _state.update { it.copy(isLoading = false) }
                _effect.emit(UserCreateContract.Effect.ShowError("Coach ma'lumotlarini qo'shishda xatolik: ${t.message}"))
            }
            .launchIn(screenModelScope)
    }
}
