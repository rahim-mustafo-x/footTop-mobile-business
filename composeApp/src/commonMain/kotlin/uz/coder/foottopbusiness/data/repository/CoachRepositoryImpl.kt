package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.CoachApiService
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.repository.CoachRepository

class CoachRepositoryImpl(
    private val api: CoachApiService,
) : CoachRepository {

    override fun getCoaches() = flow {
        emit(api.getCoaches().getOrThrow().data ?: emptyList())
    }

    override fun getCoachById(id: Long) = flow {
        emit(api.getCoachById(id).getOrThrow().data!!)
    }

    override fun createCoach(dto: CoachRequestDto) = flow {
        emit(api.createCoach(dto).getOrThrow().data!!)
    }
}
