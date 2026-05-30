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
import uz.coder.foottopbusiness.data.network.dto.tournament.PageTournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentFilterDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto

class TournamentApiService(private val client: HttpClient) {
    companion object {
        private const val TOURNAMENTS = "/v1/tournaments"
        private const val TOURNAMENTS_CREATE = "/v1/tournaments/create"
    }

    suspend fun getTournaments(
        page: Int = 0,
        size: Int = 10,
        filters: TournamentFilterDto? = null
    ): BaseResponse<PageTournamentResponseDto> =
        client.get(TOURNAMENTS) {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
                filters?.let { it ->
                    it.name?.takeIf { it.isNotBlank() }?.let { parameters.append("name", it) }
                    it.organizerId?.let { parameters.append("organizerId", it.toString()) }
                    it.startDateFrom?.let { parameters.append("startDateFrom", it) }
                    it.startDateTo?.let { parameters.append("startDateTo", it) }
                    it.sportType?.let { parameters.append("sportType", it) }
                    it.maxTeams?.let { parameters.append("maxTeams", it.toString()) }
                    it.maxEntryFee?.let { parameters.append("maxEntryFee", it.toString()) }
                    it.status?.let { parameters.append("status", it) }
                    it.address?.let { parameters.append("address", it) }
                }
            }
        }.body()

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
