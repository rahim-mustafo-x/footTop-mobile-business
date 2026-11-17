package uz.coder.foottopbusiness.domain.usecase.user

import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.repository.UserRepository

/**
 * Joriy foydalanuvchi hisobini serverda o'chiradi, so'ng lokal sessiyani tozalaydi.
 * Server o'chirishni rad etsa, sessiya saqlanib qoladi va xato yuqoriga uzatiladi.
 */
class DeleteAccountUseCase(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        val userId = userRepository.userId()
        require(userId > 0) { "Foydalanuvchi aniqlanmadi" }
        userRepository.deleteUser(userId)
        authRepository.logout()
    }
}
