package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Takroriy bronning kutilayotgan kelgusi haftalarini bir yo'la tasdiqlaydi. */
class ConfirmSeriesUseCase(private val repository: BookingRepository) {
    operator fun invoke(groupId: String) = repository.confirmSeries(groupId)
}
