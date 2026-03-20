package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import uz.coder.foottopbusiness.core.applyToken
import uz.coder.foottopbusiness.core.safeApiCall
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto

class CoachApiService(private val client: HttpClient) {
    companion object {
        private const val COACHES = "/v1/coaches"
    }

    suspend fun getCoaches(token: String): Result<BaseResponse<List<CoachResponseDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + COACHES) {
                applyToken(token)
            }
        }

    suspend fun getCoachById(token: String, id: Long): Result<BaseResponse<CoachResponseDto>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + "$COACHES/$id") {
                applyToken(token)
            }
        }

    // swagger: POST /v1/coaches/create?dto=... (query param)
    suspend fun createCoach(token: String, dto: CoachRequestDto): Result<BaseResponse<CoachResponseDto>> =
        safeApiCall {
            client.post(HttpClientFactory.BASE_URL + "$COACHES/create") {
                applyToken(token)
                url {
                    parameters.append("userId", dto.userId.toString())
                    parameters.append("specialty", dto.specialty)
                    parameters.append("experienceYears", dto.experienceYears.toString())
                    parameters.append("hourlyRate", dto.hourlyRate.toString())
                    dto.availability?.let { parameters.append("availability", it) }
                }
            }
        }
}
