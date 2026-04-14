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
import uz.coder.foottopbusiness.domain.repository.UserRepository

class UserCreateViewModel(
    private val userRepository: UserRepository
) : ScreenModel, UserCreateContract {

    private val _state = MutableStateFlow(UserCreateContract.State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UserCreateContract.Effect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: UserCreateContract.Event) {
        when (event) {
            is UserCreateContract.Event.FullNameChanged -> {
                _state.update { it.copy(fullName = event.name) }
            }
            is UserCreateContract.Event.PhoneChanged -> {
                _state.update { it.copy(phone = event.phone) }
            }
            is UserCreateContract.Event.RoleChanged -> {
                _state.update { it.copy(role = event.role) }
            }
            UserCreateContract.Event.CreateClicked -> createUser()
        }
    }

    private fun createUser() {
        val currentState = _state.value
        if (currentState.fullName.isBlank() || currentState.phone.isBlank()) {
            screenModelScope.launch {
                _effect.emit(UserCreateContract.Effect.ShowError("Iltimos, barcha maydonlarni to'ldiring"))
            }
            return
        }

        val request = UserRequestDto(
            fullName = currentState.fullName,
            phone = currentState.phone,
            username = currentState.phone, // Using phone as username for simplicity
            password = "password123" // Default password
        )

        userRepository.createUser(request)
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
