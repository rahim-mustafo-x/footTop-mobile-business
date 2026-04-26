package uz.coder.foottopbusiness.domain.usecase.admin

import uz.coder.foottopbusiness.data.network.dto.admin.CreateStaffUserDto
import uz.coder.foottopbusiness.domain.repository.AdminRepository

class CreateStaffUseCase(private val repository: AdminRepository) {
    operator fun invoke(dto: CreateStaffUserDto) = repository.createStaff(dto)
}
