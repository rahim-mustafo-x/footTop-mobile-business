package uz.coder.foottopbusiness.domain.usecase.user

import uz.coder.foottopbusiness.domain.repository.UserRepository

class UserIdUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.userId()
}