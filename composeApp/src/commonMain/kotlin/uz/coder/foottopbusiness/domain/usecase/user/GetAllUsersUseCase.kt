package uz.coder.foottopbusiness.domain.usecase.user

import uz.coder.foottopbusiness.domain.repository.UserRepository

class GetAllUsersUseCase(private val repo: UserRepository) {
    operator fun invoke() = repo.getAllUsers()
}
