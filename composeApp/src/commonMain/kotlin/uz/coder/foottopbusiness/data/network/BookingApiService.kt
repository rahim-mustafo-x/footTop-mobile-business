package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto

class BookingApiService(private val client: HttpClient) {
    suspend fun createBooking(request: BookingRequestDto): BaseResponse<BookingResponseDto> {
        return client.post("/v1/booking/create") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
