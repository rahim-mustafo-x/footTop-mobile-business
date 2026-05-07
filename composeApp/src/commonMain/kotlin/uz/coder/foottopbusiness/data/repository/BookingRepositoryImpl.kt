package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.data.network.BookingApiService
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
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
}
