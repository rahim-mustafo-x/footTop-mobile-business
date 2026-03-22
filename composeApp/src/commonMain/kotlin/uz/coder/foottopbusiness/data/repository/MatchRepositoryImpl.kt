package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.MatchApiService
import uz.coder.foottopbusiness.domain.repository.MatchRepository

class MatchRepositoryImpl(
    private val api: MatchApiService,
) : MatchRepository {

    override fun getMatches() = flow {
        emit(api.getMatches().getOrThrow().data ?: emptyList())
    }

    override fun getMatchById(id: Long) = flow {
        emit(api.getMatchById(id).getOrThrow().data!!)
    }
}
