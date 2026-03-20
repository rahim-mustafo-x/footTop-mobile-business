package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto

interface CoachRepository {
    fun getCoaches(): Flow<List<CoachResponseDto>>
    fun getCoachById(id: Long): Flow<CoachResponseDto>
    fun createCoach(dto: CoachRequestDto): Flow<CoachResponseDto>
}
