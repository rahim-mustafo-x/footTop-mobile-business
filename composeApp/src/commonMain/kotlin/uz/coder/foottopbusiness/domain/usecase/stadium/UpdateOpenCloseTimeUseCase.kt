package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

class UpdateOpenCloseTimeUseCase(private val repository: StadiumRepository) {
    operator fun invoke(id: Int, openTime: String, closeTime: String) =
        repository.updateOpenCloseTime(id, openTime, closeTime)
}
