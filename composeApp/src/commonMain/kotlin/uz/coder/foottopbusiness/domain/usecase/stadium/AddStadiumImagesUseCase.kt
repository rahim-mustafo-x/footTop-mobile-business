package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.core.platform.PickedImage
import uz.coder.foottopbusiness.domain.repository.StadiumRepository

data class AddStadiumImagesUseCase(private val stadiumRepository: StadiumRepository) {
    operator fun invoke(id: Int, images: List<PickedImage>) =
        stadiumRepository.addStadiumImages(id, images)
}
