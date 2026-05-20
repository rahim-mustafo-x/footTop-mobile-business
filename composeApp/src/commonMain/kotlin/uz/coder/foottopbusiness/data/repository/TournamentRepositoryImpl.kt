package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.network.TournamentApiService
import uz.coder.foottopbusiness.data.network.dto.tournament.TournamentRequestDto
import uz.coder.foottopbusiness.data.network.dto.tournament.PageTournamentResponseDto
import uz.coder.foottopbusiness.domain.repository.TournamentRepository

class TournamentRepositoryImpl(
    private val api: TournamentApiService,
) : TournamentRepository {

    override fun getTournaments(page: Int, size: Int) = flow {
        val response = api.getTournaments(page, size)
        if (response.success == true) {
            emit(response.data ?: PageTournamentResponseDto())
        } else {
            throw Exception(response.message ?: "Turnirlarni yuklashda xatolik")
        }
    }.catch {
        log("TournamentRepository", "getTournaments error: ${it.message}")
        throw it
    }

    override fun getTournamentById(id: Long) = flow {
        val response = api.getTournamentById(id)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Turnir topilmadi")
        } else {
            throw Exception(response.message ?: "Turnirni yuklashda xatolik")
        }
    }.catch {
        log("TournamentRepository", "getTournamentById error: ${it.message}")
        throw it
    }

    override fun createTournament(request: TournamentRequestDto) = flow {
        val response = api.createTournament(request)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Turnir yaratildi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Turnir yaratishda xatolik")
        }
    }.catch {
        log("TournamentRepository", "createTournament error: ${it.message}")
        throw it
    }
}
