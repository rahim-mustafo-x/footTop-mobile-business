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
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Foydalanuvchi ma'lumotlari topilmadi")
        } else {
            throw Exception(response.message ?: "Foydalanuvchini yuklashda xatolik")
        }
    }

    override fun createUser(dto: UserRequestDto) = flow {
        val response = api.createUser(dto)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Foydalanuvchi yaratildi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Foydalanuvchi yaratishda xatolik")
        }
    }

    override fun updateUser(id: Long, dto: UserRequestDto) = flow {
        val response = api.updateUser(id, dto)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Foydalanuvchi yangilandi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Foydalanuvchini yangilashda xatolik")
        }
    }

    override fun getAllUsers() = flow {
        val response = api.getAllUsers()
        if (response.success == true) {
            emit(response.data ?: emptyList())
        } else {
            throw Exception(response.message ?: "Foydalanuvchilarni yuklashda xatolik")
        }
    }

    override fun generatePassword() = flow {
        val response = api.generatePassword()
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Parol yaratishda xatolik")
        } else {
            throw Exception(response.message ?: "Parol yaratishda xatolik")
        }
    }

    override suspend fun userId(): Long {
        val scope = CoroutineScope(Dispatchers.IO)
        val stateIn = preferencesManager.userId.stateIn(scope)
        return stateIn.value.toLong()
    }
}
