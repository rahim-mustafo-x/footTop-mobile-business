package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.domain.repository.BookingRepository

class CreateBookingUseCase(private val repository: BookingRepository) {
    operator fun invoke(request: BookingRequestDto) = repository.createBooking(request)
}
