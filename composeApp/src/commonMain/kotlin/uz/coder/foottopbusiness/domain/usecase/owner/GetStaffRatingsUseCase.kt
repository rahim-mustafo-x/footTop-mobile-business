package uz.coder.foottopbusiness.domain.usecase.owner

import uz.coder.foottopbusiness.domain.repository.OwnerRepository

/** Egasi stadionlari haqidagi sharhlar. */
class GetStaffRatingsUseCase(private val repository: OwnerRepository) {
    operator fun invoke(stadiumId: Long? = null) = repository.getRatings(stadiumId)
}
