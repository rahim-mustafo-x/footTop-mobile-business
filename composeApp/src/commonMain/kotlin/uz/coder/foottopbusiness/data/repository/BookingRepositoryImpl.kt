package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.BookingApiService
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.booking.CancelBookingRequestDto
import uz.coder.foottopbusiness.domain.repository.BookingRepository

class BookingRepositoryImpl(private val api: BookingApiService) : BookingRepository {
    override fun createBooking(request: BookingRequestDto): Flow<BookingResponseDto> = flow {
        val response = api.createBooking(request)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Bron yaratildi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Bron qilishda xatolik")
        }
    }

    override fun getBookingsByStadiumId(stadiumId: Long, date: String): Flow<List<BookingResponseDto>> = flow {
        val response = api.getBookingsByStadiumId(stadiumId, date)
        if (response.success == true) {
            emit(response.data ?: emptyList())
        } else {
            throw Exception(response.message ?: "Ma'lumotlarni yuklashda xatolik")
        }
    }

    override fun getBookings(
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
    ): Flow<List<BookingResponseDto>> = flow {
        val response = api.getBookings(
            userId, stadiumId, matchId, startDateFrom, startDateTo,
            totalPrice, status, paymentMethod, page, size
        )
        if (response.success == true) {
            emit(response.data ?: emptyList())
        } else {
            throw Exception(response.message ?: "Ma'lumotlarni yuklashda xatolik")
        }
    }

    override fun cancelBooking(id: Long, reason: String): Flow<BookingResponseDto> = flow {
        val response = api.cancelBooking(id, CancelBookingRequestDto(reason))
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Bekor qilindi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Bekor qilishda xatolik")
        }
    }

    override fun confirmBooking(id: Long): Flow<BookingResponseDto> = flow {
        val response = api.confirmBooking(id)
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Tasdiqlandi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Tasdiqlashda xatolik")
        }
    }

    override fun rejectBooking(id: Long, reason: String): Flow<BookingResponseDto> = flow {
        val response = api.rejectBooking(id, CancelBookingRequestDto(reason))
        if (response.success == true) {
            response.data?.let { emit(it) } ?: throw Exception("Rad etildi, lekin ma'lumotlar qaytmadi")
        } else {
            throw Exception(response.message ?: "Rad etishda xatolik")
        }
    }
}
