package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.UserApiService
import uz.coder.foottopbusiness.data.network.dto.UserRequestDto
import uz.coder.foottopbusiness.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: UserApiService,
    private val preferencesManager: PreferencesManager
) : UserRepository {

    override fun getUserById(id: Long) = flow {
        val response = api.getUserById(id)
        response.data?.let { emit(it) }
    }

    override fun createUser(dto: UserRequestDto) = flow {
        val response = api.createUser(dto)
        response.data?.let { emit(it) }
    }

    override fun updateUser(id: Long, dto: UserRequestDto) = flow {
        val response = api.updateUser(id, dto)
        response.data?.let { emit(it) }
    }

    override fun getAllUsers() = flow {
        val response = api.getAllUsers()
        response.data?.let { emit(it) }
    }

    override fun generatePassword() = flow {
        val response = api.generatePassword()
        response.data?.let { emit(it) }
    }

    override suspend fun userId(): Long {
        val scope = CoroutineScope(Dispatchers.IO)
        val stateIn = preferencesManager.userId.stateIn(scope)
        return stateIn.value.toLong()
    }
}
