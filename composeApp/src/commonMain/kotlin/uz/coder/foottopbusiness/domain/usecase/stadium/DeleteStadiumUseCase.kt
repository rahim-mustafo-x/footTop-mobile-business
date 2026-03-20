package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

class DeleteStadiumUseCase(private val repository: StadiumRepository) {
    operator fun invoke(id: Int) = repository.deleteStadium(id)
}
