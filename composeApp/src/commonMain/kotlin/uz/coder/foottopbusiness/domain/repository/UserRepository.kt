package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.data.network.dto.UserRequestDto

interface UserRepository {
    fun getUserById(id: Long): Flow<UserDto>
    fun updateUser(id: Long, dto: UserRequestDto): Flow<UserDto>
    suspend fun userId(): Long
}
