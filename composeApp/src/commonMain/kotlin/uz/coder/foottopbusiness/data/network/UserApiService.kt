package uz.coder.foottopbusiness.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import uz.coder.foottopbusiness.data.network.dto.BaseResponse
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.data.network.dto.UserRequestDto

class UserApiService(private val client: HttpClient) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    companion object {
        private const val USERS = "/v1/users/"
        private const val CREATE_USER = "/v1/users/create"
        private const val GENERATE_PASSWORD = "/v1/admin/users/generate-password"
    }

    suspend fun generatePassword(): BaseResponse<String> {
        val response = client.get(GENERATE_PASSWORD)
        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun getUserById(id: Long): BaseResponse<UserDto> {
        val response = client.get(USERS) {
            url.appendPathSegments(id.toString())
        }
        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun createUser(dto: UserRequestDto): BaseResponse<UserDto> {
        val response = client.post(CREATE_USER) {
            setBody(dto)
            contentType(ContentType.Application.Json)
        }
        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun updateUser(id: Long, dto: UserRequestDto): BaseResponse<UserDto> {
        val response = client.put(USERS) {
            url {
                appendPathSegments(id.toString())
            }
            setBody(dto)
            contentType(ContentType.Application.Json)
        }
        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun getAllUsers(): BaseResponse<List<UserDto>> {
        val response = client.get(USERS.removeSuffix("/"))
        return json.decodeFromString(response.bodyAsText())
    }
}
