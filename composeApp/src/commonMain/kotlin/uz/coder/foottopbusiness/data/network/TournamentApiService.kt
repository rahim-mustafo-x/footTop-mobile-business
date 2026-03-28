package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto

class TournamentApiService(private val client: HttpClient) {
    companion object {
        private const val TOURNAMENTS = "/v1/tournaments"
        private const val TOURNAMENTS_CREATE = "/v1/tournaments/create"
    }

    suspend fun getTournaments(): BaseResponse<List<TournamentResponseDto>> =
        client.get(TOURNAMENTS).body()

    suspend fun getTournamentById(id: Long): BaseResponse<TournamentResponseDto> =
        client.get(TOURNAMENTS){
            url{
                appendPathSegments(id.toString())
            }
        }.body()

    suspend fun createTournament(request: TournamentRequestDto): BaseResponse<TournamentResponseDto> =
        client.post(TOURNAMENTS_CREATE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
