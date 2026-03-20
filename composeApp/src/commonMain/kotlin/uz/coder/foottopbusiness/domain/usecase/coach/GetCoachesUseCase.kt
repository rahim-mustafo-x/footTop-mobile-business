package uz.coder.foottopbusiness.domain.usecase.coach

import uz.coder.foottopbusiness.domain.repository.CoachRepository

class GetCoachesUseCase(private val repo: CoachRepository) {
    operator fun invoke() = repo.getCoaches()
}
