package uz.coder.foottopbusiness.domain.usecase.user

import uz.coder.foottopbusiness.domain.repository.UserRepository

class GetUserUseCase(private val repo: UserRepository) {
    operator fun invoke(id: Long) = repo.getUserById(id)
}
