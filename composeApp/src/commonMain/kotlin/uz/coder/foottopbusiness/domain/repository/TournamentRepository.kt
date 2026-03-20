package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto

interface TournamentRepository {
    fun getTournaments(): Flow<List<TournamentResponseDto>>
    fun getTournamentById(id: Long): Flow<TournamentResponseDto>
    fun createTournament(request: TournamentRequestDto): Flow<TournamentResponseDto>
}
