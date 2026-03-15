package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.StadiumApiService
import uz.coder.foottopbusiness.data.network.dto.stadium.CreateStadiumRequest
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.ImageDto
import uz.coder.foottopbusiness.data.network.dto.stadium.LocationDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.repository.StadiumRepository

class StadiumRepositoryImpl(
    private val stadiumApiService: StadiumApiService,
    private val preferencesManager: PreferencesManager
) : StadiumRepository {

    override fun createStadium(
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
    ) = flow {
        val ownerId = preferencesManager.userId.firstOrNull() ?: 0
        val token = preferencesManager.token.firstOrNull() ?: ""
        val response = stadiumApiService.createStadium(
            token,
            CreateStadiumRequest(
                name = name,
                ownerId = ownerId,
                regionId = regionId,
                districtId = districtId,
                description = description,
                location = LocationDto(),
                type = type,
                duration = duration,
                capacity = capacity,
                pricePerHour = pricePerHour,
                images = if (imageUrl.isNotBlank()) listOf(ImageDto(imageUrl)) else emptyList(),
                openTime = openTime,
                closeTime = closeTime,
            )
        )
        val data = response.getOrThrow().data ?: throw Exception(response.getOrThrow().message ?: "Xatolik yuz berdi")
        emit(data)
    }

    override fun getRegions() = flow {
        val result = stadiumApiService.getRegions().getOrThrow()
        emit(result.data ?: emptyList())
    }

    override fun getDistricts(regionId: Int) = flow {
        val result = stadiumApiService.getDistrictsByRegion(regionId).getOrThrow()
        emit(result.data ?: emptyList())
    }

    override suspend fun saveRegionId(id: Int) {
        preferencesManager.setRegionId(id)
    }

    override suspend fun saveDistrictId(id: Int) {
        preferencesManager.setDistrictId(id)
    }

    override fun getSavedRegionId(): Flow<Int> = preferencesManager.regionId

    override fun getSavedDistrictId(): Flow<Int> = preferencesManager.districtId
}
