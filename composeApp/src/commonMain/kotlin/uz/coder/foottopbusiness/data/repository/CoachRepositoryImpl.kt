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
        emit(response.data ?: emptyList())
    }.catch {
        log("CoachRepository", "getCoaches error: ${it.message}")
        emit(emptyList())
    }

    override fun getCoachById(id: Long) = flow {
        val response = api.getCoachById(id)
        emit(response.data!!)
    }.catch {
        log("CoachRepository", "getCoachById error: ${it.message}")
    }

    override fun createCoach(dto: CoachRequestDto) = flow {
        val response = api.createCoach(dto)
        emit(response.data!!)
    }.catch {
        log("CoachRepository", "createCoach error: ${it.message}")
    }
}
