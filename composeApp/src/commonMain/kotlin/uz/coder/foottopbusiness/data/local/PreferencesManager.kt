@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import kotlinx.coroutines.flow.Flow

expect class PreferencesManager {
    val token: Flow<String?>
    val refreshToken: Flow<String?>
    val authorised: Flow<Boolean>
    val userId: Flow<Int>
    val role: Flow<String?>
    val regionId: Flow<Int>
    val districtId: Flow<Int>
    val accessTokenExpiration: Flow<Long>
    val refreshTokenExpiration: Flow<Long>
    val notificationPermission: Flow<Boolean>
    val notificationPermissionDate: Flow<Long>
    suspend fun setToken(token: String)
    suspend fun setRefreshToken(token: String)
    suspend fun setAuthorised(value: Boolean)
    suspend fun setUserId(id: Int)
    suspend fun setRole(role: String)
    suspend fun setRegionId(id: Int)
    suspend fun setDistrictId(id: Int)
    suspend fun setAccessTokenExpiration(timestamp: Long)
    suspend fun setRefreshTokenExpiration(timestamp: Long)
    suspend fun setNotificationPermission(value: Boolean)
    suspend fun setNotificationPermissionDate(timestamp: Long)
    suspend fun logout()
}