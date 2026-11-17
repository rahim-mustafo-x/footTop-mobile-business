package uz.coder.foottopbusiness.domain.usecase.booking

import uz.coder.foottopbusiness.domain.repository.BookingRepository

/** Tasdiqlangan, boshlangan bronni "kelmadi" deb belgilaydi (yoki belgini olib tashlaydi). */
class MarkNoShowUseCase(private val repository: BookingRepository) {
    operator fun invoke(id: Long, noShow: Boolean) = repository.markNoShow(id, noShow)
}
