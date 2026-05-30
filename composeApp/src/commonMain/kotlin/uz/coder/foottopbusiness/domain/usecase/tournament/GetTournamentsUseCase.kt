package uz.coder.foottopbusiness.domain.usecase.tournament

import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentFilterDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class GetTournamentsUseCase(private val repo: TournamentRepository) {
    operator fun invoke(
        page: Int = 0,
        size: Int = 10,
        filters: TournamentFilterDto? = null
    ) = repo.getTournaments(page, size, filters)
}
