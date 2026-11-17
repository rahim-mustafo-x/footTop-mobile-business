package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.OwnerApiService
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.owner.OwnerReportDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockRequestDto
import uz.coder.foottopbusiness.data.network.dto.owner.StaffRatingDto
import uz.coder.foottopbusiness.domain.repository.OwnerRepository

class OwnerRepositoryImpl(private val api: OwnerApiService) : OwnerRepository {

    /** Muvaffaqiyatsiz javobda backend xabari (xato kodi) bilan istisno. */
    private fun <T> BaseResponse<T>.orThrow(fallback: String): BaseResponse<T> {
        if (success != true) throw Exception(code ?: message ?: fallback)
        return this
    }

    override fun getBlocks(from: String, to: String, stadiumId: Long?): Flow<List<StadiumBlockDto>> = flow {
        emit(api.getBlocks(from, to, stadiumId).orThrow("Ma'lumotlarni yuklashda xatolik").data ?: emptyList())
    }

    override fun createBlock(request: StadiumBlockRequestDto): Flow<StadiumBlockDto> = flow {
        val data = api.createBlock(request).orThrow("Vaqtni yopishda xatolik").data
        emit(data ?: throw Exception("Vaqt yopildi, lekin ma'lumotlar qaytmadi"))
    }

    override fun deleteBlock(id: Long): Flow<Unit> = flow {
        api.deleteBlock(id).orThrow("Vaqtni ochishda xatolik")
        emit(Unit)
    }

    override fun getOwnerReport(from: String, to: String, stadiumId: Long?): Flow<OwnerReportDto> = flow {
        val data = api.getOwnerReport(from, to, stadiumId).orThrow("Hisobotni yuklashda xatolik").data
        emit(data ?: throw Exception("Hisobot qaytmadi"))
    }

    override fun getRatings(stadiumId: Long?): Flow<List<StaffRatingDto>> = flow {
        emit(api.getRatings(stadiumId).orThrow("Sharhlarni yuklashda xatolik").data ?: emptyList())
    }

    override fun replyToRating(id: Long, reply: String): Flow<StaffRatingDto> = flow {
        val data = api.replyToRating(id, reply).orThrow("Javobni saqlashda xatolik").data
        emit(data ?: throw Exception("Javob saqlandi, lekin ma'lumotlar qaytmadi"))
    }
}
