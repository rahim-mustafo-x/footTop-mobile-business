package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.PageTournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentFilterDto
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto

interface TournamentRepository {
    fun getTournaments(
        page: Int = 0,
        size: Int = 10,
        filters: TournamentFilterDto? = null
    ): Flow<PageTournamentResponseDto>
    fun getTournamentById(id: Long): Flow<TournamentResponseDto>
    fun createTournament(request: TournamentRequestDto): Flow<TournamentResponseDto>
}
