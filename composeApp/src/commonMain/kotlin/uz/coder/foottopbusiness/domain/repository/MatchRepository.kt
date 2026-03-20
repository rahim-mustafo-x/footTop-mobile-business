package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto

interface MatchRepository {
    fun getMatches(): Flow<List<MatchResponseDto>>
    fun getMatchById(id: Long): Flow<MatchResponseDto>
}
