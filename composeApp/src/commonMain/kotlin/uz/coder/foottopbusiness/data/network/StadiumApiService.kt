package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.core.applyToken
import uz.coder.foottopbusiness.core.safeApiCall
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
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

    suspend fun createStadium(token: String, request: CreateStadiumRequest): Result<BaseResponse<StadiumResponse>> =
        safeApiCall {
            client.post(HttpClientFactory.BASE_URL + CREATE_STADIUM) {
                applyToken(token)
                setBody(request)
                contentType(ContentType.Application.Json)
            }
        }

    suspend fun getStadiums(
        token: String,
        name: String? = null,
        type: String? = null,
        ownerId: Int? = null,
        isActive: Boolean? = null,
        regionId: Int? = null,
        districtId: Int? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<BaseResponse<PageStadiumResponseDto>> = safeApiCall {
        client.get(HttpClientFactory.BASE_URL + STADIUMS) {
            applyToken(token)
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
        }
    }

    suspend fun deleteStadium(token: String, id: Int): Result<BaseResponse<Unit>> = safeApiCall {
        client.delete(HttpClientFactory.BASE_URL + "$STADIUMS/$id") {
            applyToken(token)
        }
    }

    suspend fun getRegions(token: String): Result<BaseResponse<List<RegionDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + REGIONS) {
                applyToken(token)
            }
        }

    suspend fun getDistrictsByRegion(token: String, regionId: Int): Result<BaseResponse<List<DistrictDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + DISTRICTS_BY_REGION) {
                applyToken(token)
                url { parameters.append("regionId", regionId.toString()) }
            }
        }
}
