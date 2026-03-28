package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.TournamentApiService
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class TournamentRepositoryImpl(
    private val api: TournamentApiService,
) : TournamentRepository {

    override fun getTournaments() = flow {
        val response = api.getTournaments()
        emit(response.data ?: emptyList())
    }

    override fun getTournamentById(id: Long) = flow {
        val response = api.getTournamentById(id)
        response.data?.let { emit(it) }
    }

    override fun createTournament(request: TournamentRequestDto) = flow {
        val response = api.createTournament(request)
        response.data?.let { emit(it) }
    }
}
