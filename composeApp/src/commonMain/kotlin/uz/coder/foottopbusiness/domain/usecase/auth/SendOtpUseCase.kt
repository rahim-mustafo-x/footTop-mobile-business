package uz.coder.foottopbusiness.domain.usecase.auth

import uz.coder.foottopbusiness.domain.repository.AuthRepository

data class SendOtpUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(phoneNumber: String) = authRepository.sendOtp(phoneNumber)
}