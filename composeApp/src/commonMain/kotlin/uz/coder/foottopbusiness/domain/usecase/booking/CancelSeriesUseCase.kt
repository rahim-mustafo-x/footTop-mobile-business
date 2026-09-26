package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Takroriy bronning kelgusi haftalarini bir yo'la bekor qiladi. */
class CancelSeriesUseCase(private val repository: BookingRepository) {
    operator fun invoke(groupId: String, reason: String) = repository.cancelSeries(groupId, reason)
}
