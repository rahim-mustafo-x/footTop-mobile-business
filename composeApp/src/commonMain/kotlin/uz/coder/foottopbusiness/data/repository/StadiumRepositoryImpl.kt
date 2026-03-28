package uz.coder.foottopbusiness.data.repository

import io.ktor.client.call.body
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.StadiumApiService
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.stadium.CreateStadiumRequest
import uz.coder.foottopbusiness.data.network.dto.stadium.ImageDto
import uz.coder.foottopbusiness.data.network.dto.stadium.LocationDto
import uz.coder.foottopbusiness.data.network.dto.stadium.PageStadiumResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import kotlin.time.Clock

class StadiumRepositoryImpl(
    private val stadiumApiService: StadiumApiService,
    private val preferencesManager: PreferencesManager
) : StadiumRepository {

    private fun formatToIsoDateTime(timeStr: String): String {
        if (timeStr.contains("T")) return timeStr
        
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val datePart = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
            "${datePart}T${timeStr}:00"
        } catch (_: Exception) {
            timeStr
        }
    }

    override fun createStadium(
        name: String, description: String, type: String, duration: String,
        capacity: Int, pricePerHour: Int, openTime: String, closeTime: String,
        imageUrl: String, regionId: Int, districtId: Int,
    ): Flow<StadiumResponse> = flow {
        val ownerId = preferencesManager.userId.firstOrNull() ?: 0
        val response = stadiumApiService.createStadium(
            request = CreateStadiumRequest(
                name = name, ownerId = ownerId, regionId = regionId, districtId = districtId,
                description = description, location = LocationDto(), type = type, duration = duration,
                capacity = capacity, pricePerHour = pricePerHour,
                images = if (imageUrl.isNotBlank()) listOf(ImageDto(imageUrl)) else emptyList(),
            )
        )
        val data = response.data ?: throw Exception(response.message ?: "Xatolik yuz berdi")
        
        stadiumApiService.updateOpenCloseTime(
            data.id?.toLong() ?: 0L, 
            formatToIsoDateTime(openTime), 
            formatToIsoDateTime(closeTime)
        )
        
        emit(data)
    }

    override fun updateStadium(
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
        isActive: Boolean
    ): Flow<StadiumResponse> = flow {
        val ownerId = preferencesManager.userId.firstOrNull() ?: 0
        val response = stadiumApiService.updateStadium(
            id = id.toLong(),
            request = CreateStadiumRequest(
                name = name, ownerId = ownerId, regionId = regionId, districtId = districtId,
                description = description, location = LocationDto(), type = type, duration = duration,
                capacity = capacity, pricePerHour = pricePerHour,
                images = if (imageUrl.isNotBlank()) listOf(ImageDto(imageUrl)) else emptyList(),
                isActive = isActive
            )
        )
        
        // Handle 204 No Content or success with body
        val stadiumResponse = if (response.status.value == 204) {
             StadiumResponse(id = id)
        } else {
             val baseResponse = response.body<BaseResponse<StadiumResponse>>()
             baseResponse.data ?: throw Exception(baseResponse.message ?: "Xatolik yuz berdi")
        }
        
        stadiumApiService.updateOpenCloseTime(
            id.toLong(), 
            formatToIsoDateTime(openTime), 
            formatToIsoDateTime(closeTime)
        )
        
        emit(stadiumResponse)
    }

    override fun getStadiums(
        name: String?, type: String?, isActive: Boolean?, page: Int, size: Int,
    ): Flow<PageStadiumResponseDto> = flow {
        val ownerId = preferencesManager.userId.firstOrNull() ?: 0
        val response = stadiumApiService.getStadiums(
            name = name, type = type,
            ownerId = ownerId.toLong(), isActive = isActive, page = page, size = size,
        )
        emit(response.data ?: PageStadiumResponseDto())
    }

    override fun getStadiumById(id: Int, date: String, duration: String): Flow<List<StadiumResponse>> = flow {
        val response = stadiumApiService.getStadiumById(id.toLong(), date, duration)
        emit(response.data?.content ?: emptyList())
    }

    override fun updateOpenCloseTime(id: Int, openTime: String, closeTime: String): Flow<Unit> = flow {
        stadiumApiService.updateOpenCloseTime(
            id.toLong(), 
            formatToIsoDateTime(openTime), 
            formatToIsoDateTime(closeTime)
        )
        emit(Unit)
    }

    override fun deleteStadium(id: Int) = flow {
        stadiumApiService.deleteStadium(id = id.toLong())
        emit(Unit)
    }

    override fun getRegions() = flow {
        emit(stadiumApiService.getRegions().data ?: emptyList())
    }

    override fun getDistricts(regionId: Int) = flow {
        emit(stadiumApiService.getDistrictsByRegion(regionId).data ?: emptyList())
    }

    override suspend fun saveRegionId(id: Int) = preferencesManager.setRegionId(id)
    override suspend fun saveDistrictId(id: Int) = preferencesManager.setDistrictId(id)
    override fun getSavedRegionId(): Flow<Int> = preferencesManager.regionId
    override fun getSavedDistrictId(): Flow<Int> = preferencesManager.districtId
}
