package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Stadion xodimi to'lov olinganini (PAID) yoki olinmaganini (UNPAID) belgilaydi. */
class UpdatePaymentStatusUseCase(private val repository: BookingRepository) {
    operator fun invoke(id: Long, paymentStatus: String) = repository.updatePaymentStatus(id, paymentStatus)
}
