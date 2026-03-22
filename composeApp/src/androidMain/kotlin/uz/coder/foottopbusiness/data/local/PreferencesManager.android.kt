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
        private val AUTHORISED = booleanPreferencesKey("authorised")
        private val USER_ID = intPreferencesKey("user_id")
        private val REGION_ID = intPreferencesKey("region_id")
        private val DISTRICT_ID = intPreferencesKey("district_id")
    }

    actual val token: Flow<String?> = context.dataStore.data.map { preferences ->
        normalizeBearerToken(preferences[TOKEN])
    }

    actual val authorised: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTHORISED] ?: false
    }

    actual val userId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] ?: 0
    }

    actual val regionId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REGION_ID] ?: 0
    }

    actual val districtId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DISTRICT_ID] ?: 0
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
}
