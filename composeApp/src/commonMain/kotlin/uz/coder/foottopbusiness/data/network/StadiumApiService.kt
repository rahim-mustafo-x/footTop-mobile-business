package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.SimpleResponse
import uz.coder.foottopbusiness.data.network.dto.stadium.CreateStadiumRequest
import uz.coder.foottopbusiness.data.network.dto.stadium.DistrictDto
import uz.coder.foottopbusiness.data.network.dto.stadium.PageStadiumResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.RegionDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

class StadiumApiService(private val client: HttpClient) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    companion object {
        private const val CREATE_STADIUM = "/v1/stadiums/create"
        private const val STADIUMS = "/v1/stadiums"
        private const val REGIONS = "/v1/region-districts/regions"
        private const val DISTRICTS_BY_REGION = "/v1/region-districts/districts-by-region"
    }

    private suspend inline fun <reified T> safeDecode(response: HttpResponse): T {
        val text = response.bodyAsText()
        if (text.isBlank()) {
            if (response.status.isSuccess()) {
                // If success but nobody, try to return a "success" object if type matches SimpleResponse
                if (SimpleResponse::class == T::class) {
                    return SimpleResponse(success = true, message = "Success") as T
                }
                // For other types, this might still fail if T is not nullable or doesn't have a default
            }
            throw Exception("Empty response from server with status ${response.status}")
        }
        return json.decodeFromString(text)
    }

    suspend fun createStadium(request: CreateStadiumRequest): BaseResponse<StadiumResponse> {
        val response = client.post(CREATE_STADIUM) {
            setBody(request)
            contentType(ContentType.Application.Json)
        }
        return safeDecode(response)
    }

    suspend fun updateStadium(id: Long, request: CreateStadiumRequest): HttpResponse =
        client.put(STADIUMS) {
            url {
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
    ): BaseResponse<PageStadiumResponseDto> {
        val response = client.get(STADIUMS) {
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
        return safeDecode(response)
    }

    suspend fun getStadiumById(
        id: Long,
        date: String,
        duration: String
    ): BaseResponse<List<StadiumResponse>> {
        val response = client.get(STADIUMS) {
            url {
                appendPathSegments(id.toString())
                parameters.append("date", date)
                parameters.append("duration", duration)
            }
        }
        return safeDecode(response)
    }

    suspend fun updateOpenCloseTime(
        id: Long,
        openTime: String,
        closeTime: String
    ): SimpleResponse {
        val response = client.put(STADIUMS) {
            url {
                appendPathSegments(id.toString(), openTime, closeTime)
            }
        }
        return safeDecode(response)
    }

    suspend fun deleteStadium(id: Long): SimpleResponse {
        val response = client.delete(STADIUMS) {
            url {
                appendPathSegments(id.toString())
            }
        }
        return safeDecode(response)
    }

    suspend fun getRegions(): BaseResponse<List<RegionDto>> {
        val response = client.get(REGIONS)
        return safeDecode(response)
    }

    suspend fun getDistrictsByRegion(regionId: Int): BaseResponse<List<DistrictDto>> {
        val response = client.get(DISTRICTS_BY_REGION) {
            url { parameters.append("regionId", regionId.toString()) }
        }
        return safeDecode(response)
    }
}
