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
        paymentMethod: String? = null,
        page: Int = 0,
        size: Int = DEFAULT_PAGE_SIZE
    ): Flow<List<BookingResponseDto>>
    fun cancelBooking(id: Long, reason: String): Flow<BookingResponseDto>
    fun confirmBooking(id: Long): Flow<BookingResponseDto>
    fun rejectBooking(id: Long, reason: String): Flow<BookingResponseDto>
    fun updatePaymentStatus(id: Long, paymentStatus: String): Flow<BookingResponseDto>
    fun confirmSeries(groupId: String): Flow<List<BookingResponseDto>>
    fun rejectSeries(groupId: String, reason: String): Flow<List<BookingResponseDto>>
    fun cancelSeries(groupId: String, reason: String): Flow<List<BookingResponseDto>>

    companion object {
        /** Backend ham shu qiymatni default sifatida ishlatadi. */
        const val DEFAULT_PAGE_SIZE = 50
    }
}
