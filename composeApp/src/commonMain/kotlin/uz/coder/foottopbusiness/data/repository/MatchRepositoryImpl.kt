package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.MatchApiService
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.domain.repository.MatchRepository

class MatchRepositoryImpl(
    private val api: MatchApiService,
) : MatchRepository {

    override fun getMatches(): Flow<List<MatchResponseDto>> = flow {
        val response = api.getMatches()
        emit(response.data ?: emptyList())
    }

    override fun getMatchById(id: Long): Flow<MatchResponseDto> = flow {
        val response = api.getMatchById(id)
        response.data?.let { emit(it) }
    }
}
