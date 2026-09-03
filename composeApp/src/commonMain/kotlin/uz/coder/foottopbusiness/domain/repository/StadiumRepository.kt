package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.stadium.ImageDto
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.PageStadiumResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

interface StadiumRepository {
    fun createStadium(
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
    ): Flow<StadiumResponse>

    fun updateStadium(
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
        /** Mavjud rasmlar. null bo'lsa [imageUrl] dan yasaladi (eski xatti-harakat). */
        images: List<ImageDto>? = null,
        regionId: Int,
        districtId: Int,
        isActive: Boolean = true,
        ownerId: Int? = null,
        phone: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
    ): Flow<StadiumResponse>

    fun getStadiums(
        name: String? = null,
        type: String? = null,
        isActive: Boolean? = null,
        page: Int = 0,
        size: Int = 20,
    ): Flow<PageStadiumResponseDto>

    fun getStadiumById(
        id: Int,
        date: String,
        duration: String
    ): Flow<List<StadiumResponse>>

    fun updateOpenCloseTime(
        id: Int,
        openTime: String,
        closeTime: String
    ): Flow<Unit>

    fun deleteStadium(id: Int): Flow<Unit>

    fun getRegions(): Flow<List<RegionDto>>
    fun getDistricts(regionId: Int): Flow<List<DistrictDto>>

    suspend fun saveRegionId(id: Int)
    suspend fun saveDistrictId(id: Int)
    fun getSavedRegionId(): Flow<Int>
    fun getSavedDistrictId(): Flow<Int>
}
