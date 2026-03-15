package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

data class SaveRegionIdUseCase(private val repo: StadiumRepository) {
    suspend operator fun invoke(id: Int) = repo.saveRegionId(id)
}
