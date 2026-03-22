package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.TournamentApiService
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class TournamentRepositoryImpl(
    private val api: TournamentApiService,
) : TournamentRepository {

    override fun getTournaments() = flow {
        emit(api.getTournaments().getOrThrow().data ?: emptyList())
    }

    override fun getTournamentById(id: Long) = flow {
        emit(api.getTournamentById(id).getOrThrow().data!!)
    }

    override fun createTournament(request: TournamentRequestDto) = flow {
        emit(api.createTournament(request).getOrThrow().data!!)
    }
}
