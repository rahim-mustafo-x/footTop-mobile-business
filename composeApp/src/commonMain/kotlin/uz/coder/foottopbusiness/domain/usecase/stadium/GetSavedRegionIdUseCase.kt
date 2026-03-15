package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

data class GetSavedRegionIdUseCase(private val repo: StadiumRepository) {
    operator fun invoke() = repo.getSavedRegionId()
}
