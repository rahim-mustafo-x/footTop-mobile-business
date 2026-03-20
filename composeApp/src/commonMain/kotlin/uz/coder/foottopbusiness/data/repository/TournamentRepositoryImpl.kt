package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.TournamentApiService
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class TournamentRepositoryImpl(
    private val api: TournamentApiService,
    private val prefs: PreferencesManager,
) : TournamentRepository {
    private suspend fun token() = prefs.token.firstOrNull() ?: ""

    override fun getTournaments() = flow {
        emit(api.getTournaments(token()).getOrThrow().data ?: emptyList())
    }

    override fun getTournamentById(id: Long) = flow {
        emit(api.getTournamentById(token(), id).getOrThrow().data!!)
    }

    override fun createTournament(request: TournamentRequestDto) = flow {
        emit(api.createTournament(token(), request).getOrThrow().data!!)
    }
}
