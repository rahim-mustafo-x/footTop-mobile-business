package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.booking.CancelBookingRequestDto

class BookingApiService(private val client: HttpClient) {
    suspend fun createBooking(request: BookingRequestDto): BaseResponse<BookingResponseDto> {
        return client.post("/v1/booking/create") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getBookingsByStadiumId(stadiumId: Long, date: String): BaseResponse<List<BookingResponseDto>> {
        return client.get("/v1/booking/by-stadion-id") {
            parameter("stadionId", stadiumId)
            parameter("date", date)
        }.body()
    }

    suspend fun getBookings(
        userId: Long?,
        stadiumId: Long?,
        matchId: Long?,
        startDateFrom: String?,
        startDateTo: String?,
        totalPrice: Double?,
        status: String?,
        paymentMethod: String?
    ): BaseResponse<List<BookingResponseDto>> {
        return client.get("/v1/booking") {
            parameter("userId", userId)
            parameter("stadiumId", stadiumId)
            parameter("matchId", matchId)
            parameter("startDateFrom", startDateFrom)
            parameter("startDateTo", startDateTo)
            parameter("totalPrice", totalPrice)
            parameter("status", status)
            parameter("paymentMethod", paymentMethod)
        }.body()
    }

    suspend fun cancelBooking(id: Long, request: CancelBookingRequestDto): BaseResponse<BookingResponseDto> {
        return client.patch("/v1/booking/$id/cancel") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
