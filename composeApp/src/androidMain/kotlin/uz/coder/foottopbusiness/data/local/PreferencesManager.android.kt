@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.coder.foottopbusiness.core.normalizeBearerToken

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "foot_top_business_prefs")

actual class PreferencesManager(private val context: Context) {

    companion object {
        private val TOKEN = stringPreferencesKey("token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val AUTHORISED = booleanPreferencesKey("authorised")
        private val USER_ID = intPreferencesKey("user_id")
        private val ROLE = stringPreferencesKey("role")
        private val REGION_ID = intPreferencesKey("region_id")
        private val DISTRICT_ID = intPreferencesKey("district_id")
        private val ACCESS_TOKEN_EXPIRATION = longPreferencesKey("access_token_expiration")
        private val REFRESH_TOKEN_EXPIRATION = longPreferencesKey("refresh_token_expiration")
        private val NOTIFICATION_PERMISSION = booleanPreferencesKey("notification_permission")
        private val NOTIFICATION_PERMISSION_DATE = longPreferencesKey("notification_permission_date")
    }

    actual val token: Flow<String?> = context.dataStore.data.map { preferences ->
        normalizeBearerToken(preferences[TOKEN])
    }

    actual val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        normalizeBearerToken(preferences[REFRESH_TOKEN])
    }

    actual val authorised: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTHORISED] ?: false
    }

    actual val userId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] ?: 0
    }

    actual val role: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE]
    }

    actual val regionId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REGION_ID] ?: 0
    }

    actual val districtId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DISTRICT_ID] ?: 0
    }

    actual val accessTokenExpiration: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_EXPIRATION] ?: 0L
    }

    actual val refreshTokenExpiration: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_EXPIRATION] ?: 0L
    }

    actual val notificationPermission: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_PERMISSION] ?: false
    }

    actual val notificationPermissionDate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_PERMISSION_DATE] ?: 0L
    }

    actual suspend fun setToken(token: String) {
        val cleaned = normalizeBearerToken(token)
        context.dataStore.edit { preferences ->
            if (cleaned.isNullOrEmpty()) {
                preferences.remove(TOKEN)
            } else {
                preferences[TOKEN] = cleaned
            }
        }
    }

    actual suspend fun setRefreshToken(token: String) {
        val cleaned = normalizeBearerToken(token)
        context.dataStore.edit { preferences ->
            if (cleaned.isNullOrEmpty()) {
                preferences.remove(REFRESH_TOKEN)
            } else {
                preferences[REFRESH_TOKEN] = cleaned
            }
        }
    }

    actual suspend fun setAuthorised(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTHORISED] = value
        }
    }

    actual suspend fun setUserId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = id
        }
    }

    actual suspend fun setRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[ROLE] = role
        }
    }

    actual suspend fun setRegionId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[REGION_ID] = id
        }
    }

    actual suspend fun setDistrictId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[DISTRICT_ID] = id
        }
    }

    actual suspend fun setAccessTokenExpiration(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_EXPIRATION] = timestamp
        }
    }

    actual suspend fun setRefreshTokenExpiration(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_EXPIRATION] = timestamp
        }
    }

    actual suspend fun setNotificationPermission(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION] = value
        }
    }

    actual suspend fun setNotificationPermissionDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_DATE] = timestamp
        }
    }

    actual suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        // Clear application cache
        try {
            context.cacheDir.let {
                if (it.exists() && it.isDirectory) {
                    it.deleteRecursively()
                }
            }
            context.externalCacheDir?.let {
                if (it.exists() && it.isDirectory) {
                    it.deleteRecursively()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
