package uz.coder.foottopbusiness.data.repository

import io.ktor.client.call.body
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.StadiumApiService
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.stadium.CreateStadiumRequest
import uz.coder.foottopbusiness.data.network.dto.stadium.ImageDto
import uz.coder.foottopbusiness.data.network.dto.stadium.LocationDto
import uz.coder.foottopbusiness.data.network.dto.stadium.PageStadiumResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.repository.StadiumRepository

class StadiumRepositoryImpl(
    private val stadiumApiService: StadiumApiService,
    private val preferencesManager: PreferencesManager
) : StadiumRepository {

    private fun formatToIsoDateTime(timeStr: String): String {
        if (timeStr.contains("T")) return timeStr
        
        return try {
            val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val datePart = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
            "${datePart}T${timeStr}:00"
        } catch (_: Exception) {
            timeStr
        }
    }

    override fun createStadium(
        name: String, description: String, type: String, duration: String,
        capacity: Int, pricePerHour: Int, openTime: String, closeTime: String,
        imageUrl: String, regionId: Int, districtId: Int, ownerId: Int?, phone: String?,
        latitude: Double?, longitude: Double?, address: String?,
    ): Flow<StadiumResponse> = flow {
        val finalOwnerId = ownerId ?: preferencesManager.userId.first().takeIf { it != 0 }
        val response = stadiumApiService.createStadium(
            request = CreateStadiumRequest(
                name = name, phone = phone, ownerId = finalOwnerId, regionId = regionId, districtId = districtId,
                description = description, 
                location = LocationDto(latitude = latitude, longitude = longitude, address = address),
                type = type, duration = duration,
                capacity = capacity, pricePerHour = pricePerHour,
                images = if (imageUrl.isNotBlank()) listOf(ImageDto(imageUrl)) else emptyList(),
                isActive = true
            )
        )
        val data = response.data ?: throw Exception(response.message ?: "Xatolik yuz berdi")
        
        stadiumApiService.updateOpenCloseTime(
            data.id?.toLong() ?: 0L, 
            formatToIsoDateTime(openTime), 
            formatToIsoDateTime(closeTime)
        )
        
        emit(data)
    }.catch {
        log("StadiumRepository", "createStadium error: ${it.message}")
        throw it
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
        images: List<ImageDto>?,
        regionId: Int,
        districtId: Int,
        isActive: Boolean,
        ownerId: Int?,
        phone: String?,
        latitude: Double?,
        longitude: Double?,
        address: String?,
    ): Flow<StadiumResponse> = flow {
        val response = stadiumApiService.updateStadium(
            id = id.toLong(),
            request = CreateStadiumRequest(
                name = name, phone = phone, ownerId = ownerId, regionId = regionId, districtId = districtId,
                description = description, 
                location = LocationDto(latitude = latitude, longitude = longitude, address = address),
                type = type, duration = duration,
                capacity = capacity, pricePerHour = pricePerHour,
                images = images ?: if (imageUrl.isNotBlank()) listOf(ImageDto(imageUrl)) else emptyList(),
                isActive = isActive
            )
        )

        // If 204 No Content, we construct a response from the request data
        val stadiumResponse = if (response.status.value == 204) {
             StadiumResponse(
                 id = id,
                 name = name,
                 phone = phone,
                 ownerId = ownerId,
                 description = description,
                 type = type,
                 duration = duration,
                 capacity = capacity,
                 pricePerHour = pricePerHour.toDouble(),
                 isActive = isActive,
                 openTime = openTime,
                 closeTime = closeTime
             )
        } else {
             val baseResponse: BaseResponse<StadiumResponse> = response.body()
             baseResponse.data ?: throw Exception(baseResponse.message ?: "Xatolik yuz berdi")
        }
        
        try {
            stadiumApiService.updateOpenCloseTime(
                id.toLong(), 
                formatToIsoDateTime(openTime), 
                formatToIsoDateTime(closeTime)
            )
        } catch (_: Exception) {}
        
        emit(stadiumResponse)
    }.catch {
        log("StadiumRepository", "updateStadium error: ${it.message}")
        throw it
    }

    override fun getStadiums(
        name: String?, type: String?, isActive: Boolean?, page: Int, size: Int,
    ): Flow<PageStadiumResponseDto> = flow {
        val ownerId = preferencesManager.userId.first()
        val response = stadiumApiService.getStadiums(
            name = name, type = type,
            ownerId = ownerId.toLong(), isActive = isActive, page = page, size = size,
        )
        if (response.success == true) {
            emit(response.data ?: PageStadiumResponseDto())
        } else {
            throw Exception(response.message ?: "Xatolik yuz berdi")
        }
    }.catch {
        log("StadiumRepository", "getStadiums error: ${it.message}")
        throw it
    }

    override fun getStadiumById(id: Int, date: String, duration: String): Flow<List<StadiumResponse>> = flow {
        val response = stadiumApiService.getStadiumById(id.toLong(), date, duration)
        if (response.success == true) {
            emit(response.data ?: emptyList())
        } else {
            throw Exception(response.message ?: "Xatolik yuz berdi")
        }
    }.catch {
        log("StadiumRepository", "getStadiumById error: ${it.message}")
        throw it
    }

    override fun updateOpenCloseTime(id: Int, openTime: String, closeTime: String): Flow<Unit> = flow {
        stadiumApiService.updateOpenCloseTime(
            id.toLong(), 
            formatToIsoDateTime(openTime), 
            formatToIsoDateTime(closeTime)
        )
        emit(Unit)
    }.catch {
        log("StadiumRepository", "updateOpenCloseTime error: ${it.message}")
    }

    override fun deleteStadium(id: Int) = flow {
        stadiumApiService.deleteStadium(id = id.toLong())
        emit(Unit)
    }.catch {
        log("StadiumRepository", "deleteStadium error: ${it.message}")
        throw it
    }

    override fun getRegions() = flow {
        emit(stadiumApiService.getRegions().data ?: emptyList())
    }.catch {
        log("StadiumRepository", "getRegions error: ${it.message}")
        emit(emptyList())
    }

    override fun getDistricts(regionId: Int) = flow {
        emit(stadiumApiService.getDistrictsByRegion(regionId).data ?: emptyList())
    }.catch {
        log("StadiumRepository", "getDistricts error: ${it.message}")
        emit(emptyList())
    }

    override suspend fun saveRegionId(id: Int) = preferencesManager.setRegionId(id)
    override suspend fun saveDistrictId(id: Int) = preferencesManager.setDistrictId(id)
    override fun getSavedRegionId(): Flow<Int> = preferencesManager.regionId
    override fun getSavedDistrictId(): Flow<Int> = preferencesManager.districtId
}
