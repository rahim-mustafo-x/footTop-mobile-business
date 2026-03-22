package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.UserApiService
import uz.coder.foottopbusiness.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: UserApiService,
) : UserRepository {

    override fun getUserById(id: Long) = flow {
        emit(api.getUserById(id).getOrThrow().data!!)
    }
}
