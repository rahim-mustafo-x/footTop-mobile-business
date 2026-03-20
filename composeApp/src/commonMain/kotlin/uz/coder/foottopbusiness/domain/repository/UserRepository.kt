package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.UserDto

interface UserRepository {
    fun getUserById(id: Long): Flow<UserDto>
}
