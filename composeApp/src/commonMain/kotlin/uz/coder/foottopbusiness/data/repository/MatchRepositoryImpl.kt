package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.network.MatchApiService
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.domain.repository.MatchRepository

class MatchRepositoryImpl(
    private val api: MatchApiService,
) : MatchRepository {

    override fun getMatches(): Flow<List<MatchResponseDto>> = flow {
        val response = api.getMatches()
        if (response.success == true) {
            emit(response.data ?: emptyList())
        } else {
            throw Exception(response.message ?: "O'yinlarni yuklashda xatolik")
        }
    }.catch {
        log("MatchRepository", "getMatches error: ${it.message}")
        throw it
    }

    override fun getMatchById(id: Long): Flow<MatchResponseDto> = flow {
        val response = api.getMatchById(id)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("O'yin topilmadi")
        } else {
            throw Exception(response.message ?: "O'yinni yuklashda xatolik")
        }
    }.catch {
        log("MatchRepository", "getMatchById error: ${it.message}")
        throw it
    }
}
