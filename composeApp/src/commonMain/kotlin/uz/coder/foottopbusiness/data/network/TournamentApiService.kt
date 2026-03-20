package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.core.applyToken
import uz.coder.foottopbusiness.core.safeApiCall
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto

class TournamentApiService(private val client: HttpClient) {
    companion object {
        private const val TOURNAMENTS = "/v1/tournaments"
    }

    suspend fun getTournaments(token: String): Result<BaseResponse<List<TournamentResponseDto>>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + TOURNAMENTS) {
                applyToken(token)
            }
        }

    suspend fun getTournamentById(token: String, id: Long): Result<BaseResponse<TournamentResponseDto>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + "$TOURNAMENTS/$id") {
                applyToken(token)
            }
        }

    suspend fun createTournament(token: String, request: TournamentRequestDto): Result<BaseResponse<TournamentResponseDto>> =
        safeApiCall {
            client.post(HttpClientFactory.BASE_URL + "$TOURNAMENTS/create") {
                applyToken(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
}
