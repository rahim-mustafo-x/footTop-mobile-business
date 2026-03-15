package uz.coder.foottopbusiness.domain.usecase.auth

import uz.coder.foottopbusiness.domain.repository.AuthRepository

data class IsLoginInUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.isLoginIn()
}