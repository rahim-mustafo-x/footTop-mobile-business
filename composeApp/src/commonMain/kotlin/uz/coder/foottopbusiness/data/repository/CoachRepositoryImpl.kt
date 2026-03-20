package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.CoachApiService
import uz.coder.foottopbusiness.data.network.dto.coach.CoachRequestDto
import uz.coder.foottopbusiness.domain.repository.CoachRepository

class CoachRepositoryImpl(
    private val api: CoachApiService,
    private val prefs: PreferencesManager,
) : CoachRepository {
    private suspend fun token() = prefs.token.firstOrNull() ?: ""

    override fun getCoaches() = flow {
        emit(api.getCoaches(token()).getOrThrow().data ?: emptyList())
    }

    override fun getCoachById(id: Long) = flow {
        emit(api.getCoachById(token(), id).getOrThrow().data!!)
    }

    override fun createCoach(dto: CoachRequestDto) = flow {
        emit(api.createCoach(token(), dto).getOrThrow().data!!)
    }
}
