package uz.coder.foottopbusiness.domain.usecase.stadium

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
        regionId: Int,
        districtId: Int,
        isActive: Boolean = true,
        ownerId: Int? = null,
        phone: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
    ) = repository.updateStadium(
        id, name, description, type, duration, capacity, pricePerHour, openTime, closeTime, imageUrl, regionId, districtId, isActive, ownerId, phone, latitude, longitude, address
    )
}
