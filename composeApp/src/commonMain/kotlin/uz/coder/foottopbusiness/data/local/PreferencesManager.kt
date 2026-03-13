@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import kotlinx.coroutines.flow.Flow

expect class PreferencesManager {
    val token: Flow<String>
    suspend fun setToken(token:String)
}