package uz.coder.foottopbusiness.domain.usecase.admin

import uz.coder.foottopbusiness.domain.repository.AdminRepository

class HomeUseCase(private val adminRepository: AdminRepository) {
    operator fun invoke() = adminRepository.home()
}
