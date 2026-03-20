package uz.coder.foottopbusiness.domain.usecase.match

import uz.coder.foottopbusiness.domain.repository.MatchRepository

class GetMatchesUseCase(private val repo: MatchRepository) {
    operator fun invoke() = repo.getMatches()
}
