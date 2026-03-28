package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.data.network.dto.UserRequestDto

class UserApiService(private val client: HttpClient) {
    companion object {
        private const val USERS = "/v1/users/"
    }

    suspend fun getUserById(id: Long): BaseResponse<UserDto> =
        client.get(USERS) {
            url.appendPathSegments(id.toString())
        }.body()

    suspend fun updateUser(id: Long, dto: UserRequestDto): UserDto =
        client.put(USERS) {
            url {
                appendPathSegments(id.toString())
            }
            setBody(dto)
            contentType(ContentType.Application.Json)
        }.body()
}
