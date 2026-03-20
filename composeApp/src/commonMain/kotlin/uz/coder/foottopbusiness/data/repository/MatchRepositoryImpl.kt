package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.MatchApiService
import uz.coder.foottopbusiness.domain.repository.MatchRepository

class MatchRepositoryImpl(
    private val api: MatchApiService,
    private val prefs: PreferencesManager,
) : MatchRepository {
    private suspend fun token() = prefs.token.firstOrNull() ?: ""

    override fun getMatches() = flow {
        emit(api.getMatches(token()).getOrThrow().data ?: emptyList())
    }

    override fun getMatchById(id: Long) = flow {
        emit(api.getMatchById(token(), id).getOrThrow().data!!)
    }
}
