package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

data class DeleteStadiumImageUseCase(private val stadiumRepository: StadiumRepository) {
    operator fun invoke(id: Int, url: String) =
        stadiumRepository.deleteStadiumImage(id, url)
}
