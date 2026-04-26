package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.admin.DashboardDto
import uz.coder.foottopbusiness.data.network.dto.admin.WeeklyReportDto

class AdminApiService(private val client: HttpClient) {
    companion object{
        private const val DASHBOARD_END_POINT = "/v1/admin/dashboard"
        private const val WEEKLY_REPORTS_END_POINT = "/v1/admin/dashboard/weekly"
    }
    suspend fun dashboard(): BaseResponse<DashboardDto> = client.get(DASHBOARD_END_POINT).body()
    suspend fun weeklyRepo(): BaseResponse<WeeklyReportDto> = client.get(WEEKLY_REPORTS_END_POINT).body()
}
