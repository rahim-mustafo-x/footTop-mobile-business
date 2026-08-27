package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

class GetBookingsUseCase(private val repository: BookingRepository) {
    operator fun invoke(
        userId: Long? = null,
        stadiumId: Long? = null,
        matchId: Long? = null,
        startDateFrom: String? = null,
        startDateTo: String? = null,
        totalPrice: Double? = null,
        status: String? = null,
        paymentMethod: String? = null,
        page: Int = 0,
        size: Int = BookingRepository.DEFAULT_PAGE_SIZE
    ) = repository.getBookings(
        userId, stadiumId, matchId, startDateFrom, startDateTo,
        totalPrice, status, paymentMethod, page, size
    )
}
