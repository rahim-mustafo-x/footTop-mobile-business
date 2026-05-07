package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.network.CoachApiService
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.repository.CoachRepository

class CoachRepositoryImpl(
    private val api: CoachApiService,
) : CoachRepository {

    override fun getCoaches() = flow {
        val response = api.getCoaches()
        if (response.success == true) {
            emit(response.data ?: emptyList())
        } else {
            throw Exception(response.message ?: "Coachlarni yuklashda xatolik")
        }
    }.catch {
        log("CoachRepository", "getCoaches error: ${it.message}")
        throw it
    }

    override fun getCoachById(id: Long) = flow {
        val response = api.getCoachById(id)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Coach topilmadi")
        } else {
            throw Exception(response.message ?: "Coachni yuklashda xatolik")
        }
    }.catch {
        log("CoachRepository", "getCoachById error: ${it.message}")
        throw it
    }

    override fun createCoach(dto: CoachRequestDto) = flow {
        val response = api.createCoach(dto)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Coach yaratildi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Coach yaratishda xatolik")
        }
    }.catch {
        log("CoachRepository", "createCoach error: ${it.message}")
        throw it
    }
}
