package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.UserApiService
import uz.coder.foottopbusiness.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: UserApiService,
    private val prefs: PreferencesManager,
) : UserRepository {
    private suspend fun token() = prefs.token.firstOrNull() ?: ""

    override fun getUserById(id: Long) = flow {
        emit(api.getUserById(token(), id).getOrThrow().data!!)
    }
}
