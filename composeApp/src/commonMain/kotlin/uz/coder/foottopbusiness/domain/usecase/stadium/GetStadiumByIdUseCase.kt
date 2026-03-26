package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

class GetStadiumByIdUseCase(private val repository: StadiumRepository) {
    operator fun invoke(id: Int, date: String, duration: String) =
        repository.getStadiumById(id, date, duration)
}
