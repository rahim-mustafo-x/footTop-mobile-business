package uz.coder.foottopbusiness.domain.usecase.owner

import uz.coder.foottopbusiness.domain.repository.OwnerRepository

/** Sharhga javob; bo'sh matn javobni o'chiradi. */
class ReplyToRatingUseCase(private val repository: OwnerRepository) {
    operator fun invoke(id: Long, reply: String) = repository.replyToRating(id, reply)
}
