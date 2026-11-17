package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Telefon raqam bo'yicha mijoz tarixi. */
class GetCustomerSummaryUseCase(private val repository: BookingRepository) {
    operator fun invoke(phone: String) = repository.getCustomerSummary(phone)
}
