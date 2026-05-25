package uz.coder.foottopbusiness.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.coder.foottopbusiness.core.log
import uz.coder.foottopbusiness.data.network.AdminApiService
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.data.network.dto.admin.CreateStaffUserDto
import uz.coder.foottopbusiness.domain.model.Dashboard
import uz.coder.foottopbusiness.domain.model.WeeklyReport
import uz.coder.foottopbusiness.domain.repository.AdminRepository
import uz.coder.foottopbusiness.domain.model.StadiumRevenue

class AdminRepositoryImpl(private val apiService: AdminApiService) : AdminRepository {
    override fun dashboard(): Flow<Dashboard> = flow {
        val response = apiService.dashboard()
        if (response.success == true && response.data != null) {
            val dto = response.data
            emit(
                Dashboard(
                    activeStadiumsCount = dto.activeStadiumsCount ?: 0,
                    stadiumRevenues = dto.stadiumRevenues?.mapNotNull { it ->
                        it?.let {
                            StadiumRevenue(
                                stadiumId = it.stadiumId ?: 0,
                                totalRevenue = it.totalRevenue ?: 0.0
                            )
                        }
                    } ?: emptyList(),
                    tournamentsCount = dto.tournamentsCount ?: 0,
                    usersCount = dto.usersCount ?: 0
                )
            )
        }
    }.catch {
        log("AdminRepository", "dashboard error: ${it.message}")
    }

    override fun weeklyRepo(): Flow<WeeklyReport> = flow {
        val response = apiService.weeklyRepo()
        if (response.success == true && response.data != null) {
            val dto = response.data
            emit(
                WeeklyReport(
                    bookingsGrowthPercent = dto.bookingsGrowthPercent ?: 0.0,
                    dailyRevenue = dto.dailyRevenue?.mapNotNull { dr ->
                        dr?.let {
                            uz.coder.foottopbusiness.domain.model.DailyRevenue(
                                date = it.date ?: "",
                                revenue = it.revenue ?: 0.0
                            )
                        }
                    } ?: emptyList(),
                    previousWeekBookings = dto.previousWeekBookings ?: 0,
                    totalBookings = dto.totalBookings ?: 0,
                    weekEnd = dto.weekEnd ?: "",
                    weekStart = dto.weekStart ?: "",
                    weeklyRevenue = dto.weeklyRevenue ?: 0.0
                )
            )
        }
    }.catch {
        log("AdminRepository", "weeklyRepo error: ${it.message}")
    }

    override fun createStaff(dto: CreateStaffUserDto): Flow<UserDto> = flow {
        val response = apiService.createStaff(dto)
        if (response.success == true && response.data != null) {
            emit(response.data)
        } else {
            throw Exception(response.message ?: "Xodim yaratishda xatolik")
        }
    }.catch {
        log("AdminRepository", "createStaff error: ${it.message}")
        throw it
    }
}
