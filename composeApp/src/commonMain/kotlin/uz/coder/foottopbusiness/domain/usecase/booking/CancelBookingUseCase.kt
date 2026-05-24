package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

class CancelBookingUseCase(private val repository: BookingRepository) {
    operator fun invoke(id: Long, reason: String) = repository.cancelBooking(id, reason)
}
