package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

class GetBookingsByStadiumIdUseCase(private val repository: BookingRepository) {
    operator fun invoke(stadiumId: Long, date: String) = repository.getBookingsByStadiumId(stadiumId, date)
}
