package uz.coder.foottopbusiness.domain.usecase.tournament

import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class GetTournamentsUseCase(private val repo: TournamentRepository) {
    operator fun invoke(page: Int = 0, size: Int = 10) = repo.getTournaments(page, size)
}
