package uz.coder.foottopbusiness.domain.usecase.auth

import uz.coder.foottopbusiness.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(username: String, password: String) = authRepository.staffLogin(username, password)
}