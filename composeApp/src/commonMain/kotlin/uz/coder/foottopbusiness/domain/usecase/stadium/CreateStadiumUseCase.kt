package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.domain.repository.StadiumRepository

data class CreateStadiumUseCase(private val stadiumRepository: StadiumRepository) {
    operator fun invoke(
        name: String,
        description: String,
        type: String,
        duration: String,
        capacity: Int,
        pricePerHour: Int,
        openTime: String,
        closeTime: String,
        imageUrl: String,
        regionId: Int,
        districtId: Int,
    ) = stadiumRepository.createStadium(
        name = name,
        description = description,
        type = type,
        duration = duration,
        capacity = capacity,
        pricePerHour = pricePerHour,
        openTime = openTime,
        closeTime = closeTime,
        imageUrl = imageUrl,
        regionId = regionId,
        districtId = districtId,
    )
}
