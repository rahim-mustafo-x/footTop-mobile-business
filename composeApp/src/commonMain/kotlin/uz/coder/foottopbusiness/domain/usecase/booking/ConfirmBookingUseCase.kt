package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Stadion egasi (yoki admin) kutilayotgan bronni tasdiqlaydi. */
class ConfirmBookingUseCase(private val repository: BookingRepository) {
    operator fun invoke(id: Long) = repository.confirmBooking(id)
}
