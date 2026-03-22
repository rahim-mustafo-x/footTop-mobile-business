package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import uz.coder.foottopbusiness.core.safeApiCall
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.UserDto

class UserApiService(private val client: HttpClient) {
    companion object {
        private const val USERS = "/v1/users"
    }

    suspend fun getUserById(id: Long): Result<BaseResponse<UserDto>> =
        safeApiCall {
            client.get(HttpClientFactory.BASE_URL + "$USERS/$id")
        }
}
