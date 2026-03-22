package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import uz.coder.foottopbusiness.core.safeApiCall
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto

class CoachApiService(private val client: HttpClient) {
    companion object {
        private const val COACHES = "/v1/coaches"
    }

    suspend fun getCoaches(): Result<BaseResponse<List<CoachResponseDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + COACHES)
        }

    suspend fun getCoachById(id: Long): Result<BaseResponse<CoachResponseDto>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + "$COACHES/$id")
        }

    suspend fun createCoach(dto: CoachRequestDto): Result<BaseResponse<CoachResponseDto>> =
        safeApiCall {
            client.post(HttpClientFactory.BASE_URL + "$COACHES/create") {
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
