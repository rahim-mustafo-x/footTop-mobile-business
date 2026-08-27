package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Stadion egasi (yoki admin) kutilayotgan bronni rad etadi. */
class RejectBookingUseCase(private val repository: BookingRepository) {
    operator fun invoke(id: Long, reason: String) = repository.rejectBooking(id, reason)
}
