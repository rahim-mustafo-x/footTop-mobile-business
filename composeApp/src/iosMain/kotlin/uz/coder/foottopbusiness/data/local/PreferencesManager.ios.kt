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
    }
    private val _token = MutableStateFlow(userDefaults.stringForKey(TOKEN))
    private val _authorised = MutableStateFlow(userDefaults.boolForKey(AUTHORISED))

    actual val token: Flow<String?>
        get() = _token
    actual val authorised: Flow<Boolean>
        get() = _authorised

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
}