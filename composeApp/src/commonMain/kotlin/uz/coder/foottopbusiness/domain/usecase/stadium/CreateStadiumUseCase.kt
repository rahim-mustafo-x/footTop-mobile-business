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
        ownerId: Int? = null,
        phone: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
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
        ownerId = ownerId,
        phone = phone,
        latitude = latitude,
        longitude = longitude,
        address = address
    )
}
