@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class PreferencesManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    companion object{
        private const val TOKEN = "token"
        private const val AUTHORISED = "authorised"
        private const val USER_ID = "user_id"
        private const val REGION_ID = "region_id"
        private const val DISTRICT_ID = "district_id"
    }
    private val _token = MutableStateFlow(userDefaults.stringForKey(TOKEN))
    private val _authorised = MutableStateFlow(userDefaults.boolForKey(AUTHORISED))
    private val _userId = MutableStateFlow(userDefaults.integerForKey(USER_ID).toInt())
    private val _regionId = MutableStateFlow(userDefaults.integerForKey(REGION_ID).toInt())
    private val _districtId = MutableStateFlow(userDefaults.integerForKey(DISTRICT_ID).toInt())

    actual val token: Flow<String?> get() = _token
    actual val authorised: Flow<Boolean> get() = _authorised
    actual val userId: Flow<Int> get() = _userId
    actual val regionId: Flow<Int> get() = _regionId
    actual val districtId: Flow<Int> get() = _districtId

    actual suspend fun setToken(token: String) {
        userDefaults.setObject(token, TOKEN)
        userDefaults.synchronize()
        _token.emit(token)
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
}