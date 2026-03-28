package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.EmptyData
import uz.coder.foottopbusiness.data.network.dto.stadium.CreateStadiumRequest
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.PageStadiumResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

class StadiumApiService(private val client: HttpClient) {
    companion object {
        private const val CREATE_STADIUM = "/v1/stadiums/create"
        private const val STADIUMS = "/v1/stadiums"
        private const val REGIONS = "/v1/region-districts/regions"
        private const val DISTRICTS_BY_REGION = "/v1/region-districts/districts-by-region"
    }

    suspend fun createStadium(request: CreateStadiumRequest): BaseResponse<StadiumResponse> =
        client.post(CREATE_STADIUM) {
            setBody(request)
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun updateStadium(id: Long, request: CreateStadiumRequest): HttpResponse =
        client.put(STADIUMS) {
            url{
                appendPathSegments(id.toString())
            }
            setBody(request)
            contentType(ContentType.Application.Json)
        }

    suspend fun getStadiums(
        name: String? = null,
        type: String? = null,
        ownerId: Long? = null,
        isActive: Boolean? = null,
        regionId: Int? = null,
        districtId: Int? = null,
        page: Int = 0,
        size: Int = 20,
    ): BaseResponse<PageStadiumResponseDto> = client.get(STADIUMS) {
        url {
            name?.let { parameters.append("name", it) }
            type?.let { parameters.append("type", it) }
            ownerId?.let { parameters.append("ownerId", it.toString()) }
            isActive?.let { parameters.append("isActive", it.toString()) }
            regionId?.let { parameters.append("regionId", it.toString()) }
            districtId?.let { parameters.append("districtId", it.toString()) }
            parameters.append("page", page.toString())
            parameters.append("size", size.toString())
        }
    }.body()

    suspend fun getStadiumById(
        id: Long,
        date: String,
        duration: String
    ): BaseResponse<PageStadiumResponseDto> = client.get(STADIUMS) {
        url {
            appendPathSegments(id.toString())
            parameters.append("date", date)
            parameters.append("duration", duration)
        }
    }.body()

    suspend fun updateOpenCloseTime(
        id: Long,
        openTime: String,
        closeTime: String
    ): BaseResponse<EmptyData> = client.put(STADIUMS) {
        url{
            appendPathSegments(id.toString(), openTime, closeTime)
        }
    }.body()

    suspend fun deleteStadium(id: Long): BaseResponse<EmptyData> =
        client.delete(STADIUMS){
            url{
                appendPathSegments(id.toString())
            }
        }.body()

    suspend fun getRegions(): BaseResponse<List<RegionDto>> =
        client.get(REGIONS).body()

    suspend fun getDistrictsByRegion(regionId: Int): BaseResponse<List<DistrictDto>> =
        client.get(DISTRICTS_BY_REGION) {
            url { parameters.append("regionId", regionId.toString()) }
        }.body()
}
