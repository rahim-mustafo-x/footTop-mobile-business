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
import uz.coder.foottopbusiness.data.network.dto.booking.PaymentStatusRequestDto

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
        paymentMethod: String?,
        page: Int,
        size: Int
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
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun cancelBooking(id: Long, request: CancelBookingRequestDto): BaseResponse<BookingResponseDto> {
        return client.patch("/v1/booking/$id/cancel") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /** Stadion egasi kutilayotgan bronni tasdiqlaydi. */
    suspend fun confirmBooking(id: Long): BaseResponse<BookingResponseDto> {
        return client.patch("/v1/booking/$id/confirm").body()
    }

    /** To'lov olinganini (PAID) yoki olinmaganini (UNPAID) belgilaydi. */
    suspend fun updatePaymentStatus(id: Long, request: PaymentStatusRequestDto): BaseResponse<BookingResponseDto> {
        return client.patch("/v1/booking/$id/payment-status") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /** Takroriy bronning kutilayotgan kelgusi haftalarini tasdiqlaydi. */
    suspend fun confirmSeries(groupId: String): BaseResponse<List<BookingResponseDto>> {
        return client.patch("/v1/booking/series/$groupId/confirm").body()
    }

    /** Takroriy bronning kutilayotgan kelgusi haftalarini rad etadi. */
    suspend fun rejectSeries(groupId: String, request: CancelBookingRequestDto): BaseResponse<List<BookingResponseDto>> {
        return client.patch("/v1/booking/series/$groupId/reject") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /** Takroriy bronning kelgusi haftalarini bekor qiladi. */
    suspend fun cancelSeries(groupId: String, request: CancelBookingRequestDto): BaseResponse<List<BookingResponseDto>> {
        return client.patch("/v1/booking/series/$groupId/cancel") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /** Stadion egasi kutilayotgan bronni rad etadi. */
    suspend fun rejectBooking(id: Long, request: CancelBookingRequestDto): BaseResponse<BookingResponseDto> {
        return client.patch("/v1/booking/$id/reject") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
