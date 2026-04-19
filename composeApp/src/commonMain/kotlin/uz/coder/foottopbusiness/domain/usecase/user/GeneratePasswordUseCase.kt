package uz.coder.foottopbusiness.domain.usecase.user

import uz.coder.foottopbusiness.domain.repository.UserRepository

data class GeneratePasswordUseCase(private val repository: UserRepository) {
    operator fun invoke() = repository.generatePassword()
}
