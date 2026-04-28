package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.network.TournamentApiService
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class TournamentRepositoryImpl(
    private val api: TournamentApiService,
) : TournamentRepository {

    override fun getTournaments() = flow {
        val response = api.getTournaments()
        emit(response.data ?: emptyList())
    }.catch {
        log("TournamentRepository", "getTournaments error: ${it.message}")
        emit(emptyList())
    }

    override fun getTournamentById(id: Long) = flow {
        val response = api.getTournamentById(id)
        response.data?.let { emit(it) }
    }.catch {
        log("TournamentRepository", "getTournamentById error: ${it.message}")
    }

    override fun createTournament(request: TournamentRequestDto) = flow {
        val response = api.createTournament(request)
        response.data?.let { emit(it) }
    }.catch {
        log("TournamentRepository", "createTournament error: ${it.message}")
    }
}
