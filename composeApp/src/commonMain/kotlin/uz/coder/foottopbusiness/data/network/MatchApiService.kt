package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import uz.coder.foottopbusiness.core.applyToken
import uz.coder.foottopbusiness.core.safeApiCall
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto

class MatchApiService(private val client: HttpClient) {
    companion object {
        private const val MATCHES = "/v1/matches"
    }

    suspend fun getMatches(token: String): Result<BaseResponse<List<MatchResponseDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + MATCHES) {
                applyToken(token)
                url { parameters.append("filterParams", "") }
            }
        }

    suspend fun getMatchById(token: String, id: Long): Result<BaseResponse<MatchResponseDto>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + "$MATCHES/$id") {
                applyToken(token)
            }
        }
}
