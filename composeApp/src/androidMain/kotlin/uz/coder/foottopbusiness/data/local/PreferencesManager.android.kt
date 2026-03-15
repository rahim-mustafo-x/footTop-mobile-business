@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "foot_top_business")


actual class PreferencesManager(context: Context) {
    private val applicationContext = context.applicationContext
    companion object{
        private val TOKEN = stringPreferencesKey("token")
        private val AUTHORISED = booleanPreferencesKey("authorised")
        private val USER_ID = intPreferencesKey("user_id")
        private val REGION_ID = intPreferencesKey("region_id")
        private val DISTRICT_ID = intPreferencesKey("district_id")
    }
    actual val token: Flow<String?>
        get() = applicationContext.dataStore.data.map { it[TOKEN] }
    actual val authorised: Flow<Boolean>
        get() = applicationContext.dataStore.data.map { it[AUTHORISED] ?: false }
    actual val userId: Flow<Int>
        get() = applicationContext.dataStore.data.map { it[USER_ID] ?: 0 }
    actual val regionId: Flow<Int>
        get() = applicationContext.dataStore.data.map { it[REGION_ID] ?: 0 }
    actual val districtId: Flow<Int>
        get() = applicationContext.dataStore.data.map { it[DISTRICT_ID] ?: 0 }

    actual suspend fun setToken(token: String) {
        applicationContext.dataStore.edit { it[TOKEN] = token }
    }
    actual suspend fun setAuthorised(value: Boolean) {
        applicationContext.dataStore.edit { it[AUTHORISED] = value }
    }
    actual suspend fun setUserId(id: Int) {
        applicationContext.dataStore.edit { it[USER_ID] = id }
    }
    actual suspend fun setRegionId(id: Int) {
        applicationContext.dataStore.edit { it[REGION_ID] = id }
    }
    actual suspend fun setDistrictId(id: Int) {
        applicationContext.dataStore.edit { it[DISTRICT_ID] = id }
    }
}