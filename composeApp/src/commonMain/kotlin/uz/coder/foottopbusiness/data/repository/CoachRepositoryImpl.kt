package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.CoachApiService
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.repository.CoachRepository

class CoachRepositoryImpl(
    private val api: CoachApiService,
) : CoachRepository {

    override fun getCoaches() = flow {
        val response = api.getCoaches()
        emit(response.data ?: emptyList())
    }

    override fun getCoachById(id: Long) = flow {
        val response = api.getCoachById(id)
        emit(response.data!!)
    }

    override fun createCoach(dto: CoachRequestDto) = flow {
        val response = api.createCoach(dto)
        emit(response.data!!)
    }
}
