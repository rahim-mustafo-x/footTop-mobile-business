package uz.coder.foottopbusiness.domain.usecase.admin

import uz.coder.foottopbusiness.domain.repository.AdminRepository

class WeeklyReportUseCase(private val adminApiService: AdminRepository) {
    operator fun invoke() = adminApiService.weeklyRepo()
}