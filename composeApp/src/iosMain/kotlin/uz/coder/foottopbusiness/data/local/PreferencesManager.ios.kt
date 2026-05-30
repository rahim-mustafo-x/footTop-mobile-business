@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults
import uz.coder.foottopbusiness.core.normalizeBearerToken

actual class PreferencesManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    companion object{
        private const val TOKEN = "token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val AUTHORISED = "authorised"
        private const val USER_ID = "user_id"
        private const val ROLE = "role"
        private const val REGION_ID = "region_id"
        private const val DISTRICT_ID = "district_id"
        private const val ACCESS_TOKEN_EXPIRATION = "access_token_expiration"
        private const val REFRESH_TOKEN_EXPIRATION = "refresh_token_expiration"
        private const val NOTIFICATION_PERMISSION = "notification_permission"
        private const val NOTIFICATION_PERMISSION_DATE = "notification_permission_date"
    }
    private val _token = MutableStateFlow(normalizeBearerToken(userDefaults.stringForKey(TOKEN)))
    private val _refreshToken = MutableStateFlow(normalizeBearerToken(userDefaults.stringForKey(REFRESH_TOKEN)))
    private val _authorised = MutableStateFlow(userDefaults.boolForKey(AUTHORISED))
    private val _userId = MutableStateFlow(userDefaults.integerForKey(USER_ID).toInt())
    private val _role = MutableStateFlow(userDefaults.stringForKey(ROLE))
    private val _regionId = MutableStateFlow(userDefaults.integerForKey(REGION_ID).toInt())
    private val _districtId = MutableStateFlow(userDefaults.integerForKey(DISTRICT_ID).toInt())
    private val _accessTokenExpiration = MutableStateFlow(userDefaults.objectForKey(ACCESS_TOKEN_EXPIRATION) as? Long ?: 0L)
    private val _refreshTokenExpiration = MutableStateFlow(userDefaults.objectForKey(REFRESH_TOKEN_EXPIRATION) as? Long ?: 0L)
    private val _notificationPermission = MutableStateFlow(userDefaults.boolForKey(NOTIFICATION_PERMISSION))
    private val _notificationPermissionDate = MutableStateFlow(userDefaults.objectForKey(NOTIFICATION_PERMISSION_DATE) as? Long ?: 0L)

    actual val token: Flow<String?> get() = _token
    actual val refreshToken: Flow<String?> get() = _refreshToken
    actual val authorised: Flow<Boolean> get() = _authorised
    actual val userId: Flow<Int> get() = _userId
    actual val role: Flow<String?> get() = _role
    actual val regionId: Flow<Int> get() = _regionId
    actual val districtId: Flow<Int> get() = _districtId
    actual val accessTokenExpiration: Flow<Long> get() = _accessTokenExpiration
    actual val refreshTokenExpiration: Flow<Long> get() = _refreshTokenExpiration
    actual val notificationPermission: Flow<Boolean> get() = _notificationPermission
    actual val notificationPermissionDate: Flow<Long> get() = _notificationPermissionDate

    actual suspend fun setToken(token: String) {
        val cleaned = normalizeBearerToken(token)
        if (cleaned.isNullOrEmpty()) {
            userDefaults.removeObjectForKey(TOKEN)
        } else {
            userDefaults.setObject(cleaned, TOKEN)
        }
        userDefaults.synchronize()
        _token.emit(cleaned)
    }

    actual suspend fun setRefreshToken(token: String) {
        val cleaned = normalizeBearerToken(token)
        if (cleaned.isNullOrEmpty()) {
            userDefaults.removeObjectForKey(REFRESH_TOKEN)
        } else {
            userDefaults.setObject(cleaned, REFRESH_TOKEN)
        }
        userDefaults.synchronize()
        _refreshToken.emit(cleaned)
    }

    actual suspend fun setAuthorised(value: Boolean) {
        userDefaults.setBool(value, AUTHORISED)
        userDefaults.synchronize()
        _authorised.emit(value)
    }

    actual suspend fun setUserId(id: Int) {
        userDefaults.setInteger(id.toLong(), USER_ID)
        userDefaults.synchronize()
        _userId.emit(id)
    }

    actual suspend fun setRole(role: String) {
        userDefaults.setObject(role, ROLE)
        userDefaults.synchronize()
        _role.emit(role)
    }

    actual suspend fun setRegionId(id: Int) {
        userDefaults.setInteger(id.toLong(), REGION_ID)
        userDefaults.synchronize()
        _regionId.emit(id)
    }

    actual suspend fun setDistrictId(id: Int) {
        userDefaults.setInteger(id.toLong(), DISTRICT_ID)
        userDefaults.synchronize()
        _districtId.emit(id)
    }

    actual suspend fun setAccessTokenExpiration(timestamp: Long) {
        userDefaults.setObject(timestamp, ACCESS_TOKEN_EXPIRATION)
        userDefaults.synchronize()
        _accessTokenExpiration.emit(timestamp)
    }

    actual suspend fun setRefreshTokenExpiration(timestamp: Long) {
        userDefaults.setObject(timestamp, REFRESH_TOKEN_EXPIRATION)
        userDefaults.synchronize()
        _refreshTokenExpiration.emit(timestamp)
    }

    actual suspend fun setNotificationPermission(value: Boolean) {
        userDefaults.setBool(value, NOTIFICATION_PERMISSION)
        userDefaults.synchronize()
        _notificationPermission.emit(value)
    }

    actual suspend fun setNotificationPermissionDate(timestamp: Long) {
        userDefaults.setObject(timestamp, NOTIFICATION_PERMISSION_DATE)
        userDefaults.synchronize()
        _notificationPermissionDate.emit(timestamp)
    }

    actual suspend fun logout() {
        userDefaults.removeObjectForKey(TOKEN)
        userDefaults.removeObjectForKey(REFRESH_TOKEN)
        userDefaults.setBool(false, AUTHORISED)
        userDefaults.setInteger(0, USER_ID)
        userDefaults.removeObjectForKey(ROLE)
        userDefaults.setInteger(0, REGION_ID)
        userDefaults.setInteger(0, DISTRICT_ID)
        userDefaults.removeObjectForKey(ACCESS_TOKEN_EXPIRATION)
        userDefaults.removeObjectForKey(REFRESH_TOKEN_EXPIRATION)
        userDefaults.synchronize()

        _token.emit(null)
        _refreshToken.emit(null)
        _authorised.emit(false)
        _userId.emit(0)
        _role.emit(null)
        _regionId.emit(0)
        _districtId.emit(0)
        _accessTokenExpiration.emit(0L)
        _refreshTokenExpiration.emit(0L)
    }
}
