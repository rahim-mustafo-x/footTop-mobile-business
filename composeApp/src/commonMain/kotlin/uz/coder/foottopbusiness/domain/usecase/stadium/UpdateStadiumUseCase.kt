package uz.coder.foottopbusiness.domain.usecase.stadium

import uz.coder.foottopbusiness.data.network.dto.stadium.ImageDto
import uz.coder.foottopbusiness.domain.repository.StadiumRepository

class UpdateStadiumUseCase(private val repository: StadiumRepository) {
    operator fun invoke(
        id: Int,
        name: String,
        description: String,
        type: String,
        duration: String,
        capacity: Int,
        pricePerHour: Int,
        openTime: String,
        closeTime: String,
        imageUrl: String,
        images: List<ImageDto>? = null,
        regionId: Int,
        districtId: Int,
        isActive: Boolean = true,
        ownerId: Int? = null,
        phone: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
    ) = repository.updateStadium(
        id = id,
        name = name,
        description = description,
        type = type,
        duration = duration,
        capacity = capacity,
        pricePerHour = pricePerHour,
        openTime = openTime,
        closeTime = closeTime,
        imageUrl = imageUrl,
        images = images,
        regionId = regionId,
        districtId = districtId,
        isActive = isActive,
        ownerId = ownerId,
        phone = phone,
        latitude = latitude,
        longitude = longitude,
        address = address
    )
}
