package uz.coder.foottopbusiness.domain.usecase.admin

import uz.coder.foottopbusiness.domain.repository.AdminRepository

class DashboardUseCase(private val adminApiService: AdminRepository) {
    operator fun invoke() = adminApiService.dashboard()
}