package uz.coder.foottopbusiness.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.coder.foottopbusiness.domain.model.Dashboard
import uz.coder.foottopbusiness.domain.model.WeeklyReport

interface AdminRepository {
    fun dashboard(): Flow<Dashboard>
    fun weeklyRepo(): Flow<WeeklyReport>
}