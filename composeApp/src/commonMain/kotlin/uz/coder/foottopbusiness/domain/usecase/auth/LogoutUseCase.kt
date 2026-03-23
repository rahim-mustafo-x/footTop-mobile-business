package uz.coder.foottopbusiness.domain.usecase.auth

import uz.coder.foottopbusiness.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.logout()
}
