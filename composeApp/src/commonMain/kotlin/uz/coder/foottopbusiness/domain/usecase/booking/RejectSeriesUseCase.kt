package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Takroriy bronning kutilayotgan kelgusi haftalarini bir yo'la rad etadi. */
class RejectSeriesUseCase(private val repository: BookingRepository) {
    operator fun invoke(groupId: String, reason: String) = repository.rejectSeries(groupId, reason)
}
