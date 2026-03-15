package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

data class GetDistrictsUseCase(private val repo: StadiumRepository) {
    operator fun invoke(regionId: Int) = repo.getDistricts(regionId)
}
