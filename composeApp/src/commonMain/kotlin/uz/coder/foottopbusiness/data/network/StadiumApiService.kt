package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.core.safeApiCall
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.stadium.CreateStadiumRequest
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

class StadiumApiService(private val client: HttpClient) {
    companion object {
        private const val CREATE_STADIUM = "/v1/stadiums/create"
        private const val REGIONS = "/v1/region-districts/regions"
        private const val DISTRICTS_BY_REGION = "/v1/region-districts/districts-by-region"
    }

    suspend fun createStadium(token: String, request: CreateStadiumRequest): Result<BaseResponse<StadiumResponse>> =
        safeApiCall {
            client.post(HttpClientFactory.BASE_URL + CREATE_STADIUM) {
                bearerAuth(token)
                setBody(request)
                contentType(ContentType.Application.Json)
            }
        }

    suspend fun getRegions(): Result<BaseResponse<List<RegionDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + REGIONS)
        }

    suspend fun getDistrictsByRegion(regionId: Int): Result<BaseResponse<List<DistrictDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + DISTRICTS_BY_REGION) {
                url { parameters.append("regionId", regionId.toString()) }
            }
        }
}
