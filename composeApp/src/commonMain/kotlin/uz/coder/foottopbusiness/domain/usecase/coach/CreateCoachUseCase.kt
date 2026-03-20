package uz.coder.foottopbusiness.domain.usecase.coach

import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.repository.CoachRepository

data class CreateCoachUseCase(private val repository: CoachRepository) {
    operator fun invoke(dto: CoachRequestDto) = repository.createCoach(dto)
}
