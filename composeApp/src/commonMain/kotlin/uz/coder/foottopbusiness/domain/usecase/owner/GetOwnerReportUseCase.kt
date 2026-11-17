package uz.coder.foottopbusiness.domain.usecase.owner

import uz.coder.foottopbusiness.domain.repository.OwnerRepository

/** Davr bo'yicha egasi hisoboti (ko'pi bilan 92 kun). */
class GetOwnerReportUseCase(private val repository: OwnerRepository) {
    operator fun invoke(from: String, to: String, stadiumId: Long? = null) = repository.getOwnerReport(from, to, stadiumId)
}
