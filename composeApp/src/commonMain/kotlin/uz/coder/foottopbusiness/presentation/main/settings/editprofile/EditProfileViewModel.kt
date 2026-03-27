package uz.coder.foottopbusiness.presentation.main.settings.editprofile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.UserRequestDto
import uz.coder.foottopbusiness.domain.repository.UserRepository
import uz.coder.foottopbusiness.domain.usecase.user.UserIdUseCase

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val getUserIdUseCase: UserIdUseCase
) : BaseViewModel<EditProfileContract.State, EditProfileContract.Effect, EditProfileContract.Event>(
    initialState = EditProfileContract.State()
) {
    init {
        loadUser()
    }

    private fun loadUser() {

        viewModelScope.launch {
        val userId = getUserIdUseCase()
            executeAsync(onLoading = {
                updateState { copy(isLoading = true) }
            }, onError = {
                updateState { copy(isLoading = false) }
            }, onSuccess = {
                it.collect { user ->
                    updateState {
                        copy(
                            isLoading = false,
                            user = user,
                            username = user.username ?: "",
                            fullName = user.fullName ?: "",
                            phone = user.phone ?: "",
                            location = user.location ?: "",
                        )
                    }
                }
            }) {
                userRepository.getUserById(userId)
            }
        }
    }

    override fun handleEvent(event: EditProfileContract.Event) {
        when (event) {
            is EditProfileContract.Event.UsernameChanged -> updateState { copy(username = event.value) }
            is EditProfileContract.Event.FullNameChanged -> updateState { copy(fullName = event.value) }
            is EditProfileContract.Event.LocationChanged -> updateState { copy(location = event.value) }
            EditProfileContract.Event.Save -> save()
        }
    }

    private fun save() {
        val s = state.value
        val userId = s.user?.id ?: return
        viewModelScope.launch {
            updateState { copy(isSaving = true) }
            userRepository.updateUser(
                id = userId,
                dto = UserRequestDto(
                    username = s.username,
                    fullName = s.fullName,
                    phone = s.phone,
                    location = s.location,
                )
            ).catch { e ->
                updateState { copy(isSaving = false) }
                sendEffect(EditProfileContract.Effect.ShowError(e.message ?: "Xatolik yuz berdi"))
            }.collect {
                updateState { copy(isSaving = false) }
                sendEffect(EditProfileContract.Effect.SavedSuccessfully)
            }
        }
    }
}