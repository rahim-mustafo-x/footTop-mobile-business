package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.owner.OwnerReportDto
import uz.coder.foottopbusiness.data.network.dto.owner.RatingReplyRequestDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockDto
import uz.coder.foottopbusiness.data.network.dto.owner.StadiumBlockRequestDto
import uz.coder.foottopbusiness.data.network.dto.owner.StaffRatingDto

/** Stadion egasi asboblari: yopilgan vaqt, hisobot, sharhlarga javob. */
class OwnerApiService(private val client: HttpClient) {
    companion object {
        private const val BLOCKS = "/v1/stadium-blocks"
        private const val OWNER_REPORT = "/v1/admin/dashboard/owner-report"
        private const val OWNER_RATINGS = "/v1/stadium-ratings/owner"
    }

    /** [from]..[to] (yyyy-MM-dd) kunlariga tushadigan yopilgan vaqtlar; backend xodimga qarab cheklaydi. */
    suspend fun getBlocks(from: String, to: String, stadiumId: Long?): BaseResponse<List<StadiumBlockDto>> =
        client.get(BLOCKS) {
            parameter("from", from)
            parameter("to", to)
            parameter("stadiumId", stadiumId)
        }.body()

    suspend fun createBlock(request: StadiumBlockRequestDto): BaseResponse<StadiumBlockDto> =
        client.post(BLOCKS) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteBlock(id: Long): BaseResponse<JsonElement> = client.delete("$BLOCKS/$id").body()

    suspend fun getOwnerReport(from: String, to: String, stadiumId: Long?): BaseResponse<OwnerReportDto> =
        client.get(OWNER_REPORT) {
            parameter("from", from)
            parameter("to", to)
            parameter("stadiumId", stadiumId)
        }.body()

    suspend fun getRatings(stadiumId: Long?): BaseResponse<List<StaffRatingDto>> =
        client.get(OWNER_RATINGS) {
            parameter("stadiumId", stadiumId)
        }.body()

    suspend fun replyToRating(id: Long, reply: String): BaseResponse<StaffRatingDto> =
        client.put("/v1/stadium-ratings/$id/reply") {
            contentType(ContentType.Application.Json)
            setBody(RatingReplyRequestDto(reply))
        }.body()
}
