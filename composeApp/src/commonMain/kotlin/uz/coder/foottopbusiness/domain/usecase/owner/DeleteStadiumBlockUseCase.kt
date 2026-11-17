package uz.coder.foottopbusiness.domain.usecase.owner

import uz.coder.foottopbusiness.domain.repository.OwnerRepository

/** Yopilgan vaqtni qayta ochadi. */
class DeleteStadiumBlockUseCase(private val repository: OwnerRepository) {
    operator fun invoke(id: Long) = repository.deleteBlock(id)
}
