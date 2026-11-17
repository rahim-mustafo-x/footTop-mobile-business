package uz.coder.foottopbusiness.domain.usecase.owner

import uz.coder.foottopbusiness.domain.repository.OwnerRepository

/** Kunlar oralig'idagi yopilgan vaqtlar. */
class GetStadiumBlocksUseCase(private val repository: OwnerRepository) {
    operator fun invoke(from: String, to: String, stadiumId: Long? = null) = repository.getBlocks(from, to, stadiumId)
}
