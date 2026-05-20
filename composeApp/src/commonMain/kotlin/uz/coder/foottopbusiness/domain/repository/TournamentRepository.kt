package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto

import uz.coder.foottopbusiness.data.network.dto.tournament.PageTournamentResponseDto

interface TournamentRepository {
    fun getTournaments(page: Int = 0, size: Int = 10): Flow<PageTournamentResponseDto>
    fun getTournamentById(id: Long): Flow<TournamentResponseDto>
    fun createTournament(request: TournamentRequestDto): Flow<TournamentResponseDto>
}
