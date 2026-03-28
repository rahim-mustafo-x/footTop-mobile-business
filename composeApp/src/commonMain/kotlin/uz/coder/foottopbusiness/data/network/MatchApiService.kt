package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto

class MatchApiService(private val client: HttpClient) {
    companion object {
        private const val MATCHES = "/v1/matches"
    }

    suspend fun getMatches(): BaseResponse<List<MatchResponseDto>> =
        client.get(MATCHES) {
            url { parameters.append("filterParams", "") }
        }.body()

    suspend fun getMatchById(id: Long): BaseResponse<MatchResponseDto> =
        client.get(MATCHES){
            url{
                appendPathSegments(id.toString())
            }
        }.body()
}
