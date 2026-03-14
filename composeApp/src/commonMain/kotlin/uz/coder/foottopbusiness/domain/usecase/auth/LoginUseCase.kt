package uz.coder.foottopbusiness.domain.usecase.auth

import uz.coder.foottopbusiness.domain.repository.AuthRepository

data class LoginUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(phoneNumber: String, otp: String) = authRepository.login(phoneNumber, otp)
}