package uz.coder.foottopbusiness.domain.usecase.owner

import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockRequestDto
import uz.coder.foottopbusiness.domain.repository.OwnerRepository

/** Stadion vaqtini yopadi (ta'mir, tadbir...). */
class CreateStadiumBlockUseCase(private val repository: OwnerRepository) {
    operator fun invoke(request: StadiumBlockRequestDto) = repository.createBlock(request)
}
