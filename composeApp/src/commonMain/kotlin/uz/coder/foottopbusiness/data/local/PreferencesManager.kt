@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import kotlinx.coroutines.flow.Flow

expect class PreferencesManager {
    val token: Flow<String?>
    val authorised: Flow<Boolean>
    val userId: Flow<Int>
    val regionId: Flow<Int>
    val districtId: Flow<Int>
    suspend fun setToken(token: String)
    suspend fun setAuthorised(value: Boolean)
    suspend fun setUserId(id: Int)
    suspend fun setRegionId(id: Int)
    suspend fun setDistrictId(id: Int)
}