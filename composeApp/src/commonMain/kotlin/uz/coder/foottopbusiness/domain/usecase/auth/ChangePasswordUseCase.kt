package uz.coder.foottopbusiness.domain.usecase.auth

import uz.coder.foottopbusiness.domain.repository.AuthRepository

class ChangePasswordUseCase(private val repository: AuthRepository) {
    operator fun invoke(oldPassword: String, newPassword: String) =
        repository.changePassword(oldPassword, newPassword)
}
