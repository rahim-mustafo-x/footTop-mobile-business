package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.UserApiService
import uz.coder.foottopbusiness.domain.repository.UserRepository
import uz.coder.foottopbusiness.data.network.dto.UserDto

class UserRepositoryImpl(
    private val api: UserApiService,
) : UserRepository {

    override fun getUserById(id: Long) = flow {
        val result = api.getUserById(id)
        result.onSuccess { response ->
            response.data?.let { emit(it) }
        }.onFailure {
            throw it
        }
    }
}
