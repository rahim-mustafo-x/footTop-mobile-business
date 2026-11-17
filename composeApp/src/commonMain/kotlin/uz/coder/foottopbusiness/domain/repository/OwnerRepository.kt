package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.owner.OwnerReportDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockRequestDto
import uz.coder.foottopbusiness.data.network.dto.owner.StaffRatingDto

interface OwnerRepository {
    fun getBlocks(from: String, to: String, stadiumId: Long? = null): Flow<List<StadiumBlockDto>>
    fun createBlock(request: StadiumBlockRequestDto): Flow<StadiumBlockDto>
    fun deleteBlock(id: Long): Flow<Unit>
    fun getOwnerReport(from: String, to: String, stadiumId: Long? = null): Flow<OwnerReportDto>
    fun getRatings(stadiumId: Long? = null): Flow<List<StaffRatingDto>>
    fun replyToRating(id: Long, reply: String): Flow<StaffRatingDto>
}
