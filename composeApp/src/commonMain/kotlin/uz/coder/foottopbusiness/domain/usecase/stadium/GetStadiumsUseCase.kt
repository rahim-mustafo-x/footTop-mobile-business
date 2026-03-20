package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

class GetStadiumsUseCase(private val repository: StadiumRepository) {
    operator fun invoke(
        name: String? = null,
        type: String? = null,
        isActive: Boolean? = null,
        page: Int = 0,
        size: Int = 20,
    ) = repository.getStadiums(name, type, isActive, page, size)
}
