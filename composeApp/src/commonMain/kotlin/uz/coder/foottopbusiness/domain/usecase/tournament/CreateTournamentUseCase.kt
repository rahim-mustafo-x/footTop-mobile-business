package uz.coder.foottopbusiness.domain.usecase.tournament

import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

data class CreateTournamentUseCase(private val repository: TournamentRepository) {
    operator fun invoke(request: TournamentRequestDto) = repository.createTournament(request)
}
