@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package uz.coder.foottopbusiness.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class PreferencesManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    companion object{
        private const val TOKEN = "token"
    }
    private val _token = MutableStateFlow(userDefaults.stringForKey(TOKEN)?:"")
    actual val token: Flow<String>
        get() = _token

    actual suspend fun setToken(token: String) {
        userDefaults.setObject(token, TOKEN)
        userDefaults.synchronize()
        _token.emit(token)
    }
}