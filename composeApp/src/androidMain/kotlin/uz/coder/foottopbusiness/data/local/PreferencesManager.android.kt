@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
    }
    actual val token: Flow<String?>
        get() = applicationContext.dataStore.data.map { preferences->
            preferences[TOKEN]
        }
    actual val authorised: Flow<Boolean>
        get() = applicationContext.dataStore.data.map { preferences ->
            preferences[AUTHORISED] ?: false
        }

    actual suspend fun setToken(token: String) {
        applicationContext.dataStore.edit { preferences ->
            preferences[TOKEN] = token
        }
    }

    actual suspend fun setAuthorised(value: Boolean) {
        applicationContext.dataStore.edit { preferences ->
            preferences[AUTHORISED] = value
        }
    }
}