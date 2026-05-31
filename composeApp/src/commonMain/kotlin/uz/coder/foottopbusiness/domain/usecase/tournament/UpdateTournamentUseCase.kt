package uz.coder.foottopbusiness.domain.usecase.tournament

import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class UpdateTournamentUseCase(private val repository: TournamentRepository) {
    operator fun invoke(id: Long, request: TournamentRequestDto) = repository.updateTournament(id, request)
}
