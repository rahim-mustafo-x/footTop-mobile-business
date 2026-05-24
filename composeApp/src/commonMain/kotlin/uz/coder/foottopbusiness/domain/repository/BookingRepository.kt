package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.booking.CancelBookingRequestDto

interface BookingRepository {
    fun createBooking(request: BookingRequestDto): Flow<BookingResponseDto>
    fun getBookingsByStadiumId(stadiumId: Long, date: String): Flow<List<BookingResponseDto>>
    fun getBookings(
        userId: Long? = null,
        stadiumId: Long? = null,
        matchId: Long? = null,
        startDateFrom: String? = null,
        startDateTo: String? = null,
        totalPrice: Double? = null,
        status: String? = null,
        paymentMethod: String? = null
    ): Flow<List<BookingResponseDto>>
    fun cancelBooking(id: Long, reason: String): Flow<BookingResponseDto>
}
