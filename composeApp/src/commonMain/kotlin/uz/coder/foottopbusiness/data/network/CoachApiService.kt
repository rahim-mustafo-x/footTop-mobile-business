package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.appendPathSegments
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.CoachResponseDto
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto

class CoachApiService(private val client: HttpClient) {
    companion object {
        private const val COACHES = "/v1/coaches"
        private const val COACHES_CREATE = "/v1/coaches/create"
    }

    suspend fun getCoaches(): BaseResponse<List<CoachResponseDto>> =
        client.get(COACHES).body()

    suspend fun getCoachById(id: Long): BaseResponse<CoachResponseDto> =
        client.get(COACHES){
            url {
                appendPathSegments( id.toString())
            }
        }.body()

    suspend fun createCoach(dto: CoachRequestDto): BaseResponse<CoachResponseDto> =
        client.post(COACHES_CREATE) {
            url {
                parameters.append("userId", dto.userId.toString())
                parameters.append("specialty", dto.specialty)
                parameters.append("experienceYears", dto.experienceYears.toString())
                parameters.append("hourlyRate", dto.hourlyRate.toString())
                dto.availability?.let { parameters.append("availability", it) }
            }
        }.body()
}
