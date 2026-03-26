package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
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
