package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.TournamentApiService
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class TournamentRepositoryImpl(
    private val api: TournamentApiService,
) : TournamentRepository {

    override fun getTournaments() = flow {
        val result = api.getTournaments()
        result.onSuccess { response ->
            emit(response.data ?: emptyList())
        }.onFailure {
            throw it
        }
    }

    override fun getTournamentById(id: Long) = flow {
        val result = api.getTournamentById(id)
        result.onSuccess { response ->
            response.data?.let { emit(it) }
        }.onFailure {
            throw it
        }
    }

    override fun createTournament(request: TournamentRequestDto) = flow {
        val result = api.createTournament(request)
        result.onSuccess { response ->
            response.data?.let { emit(it) }
        }.onFailure {
            throw it
        }
    }
}
