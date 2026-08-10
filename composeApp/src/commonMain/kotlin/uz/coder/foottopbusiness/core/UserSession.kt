package uz.coder.foottopbusiness.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.domain.model.UserRole

class UserSession(private val preferencesManager: PreferencesManager) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _user = MutableStateFlow<UserDto?>(null)
    val user = _user.asStateFlow()

    private val _role = MutableStateFlow(UserRole.UNKNOWN)
    val role = _role.asStateFlow()

    init {
        // Sessiya tugaganda (qo'lda chiqish yoki 401) xotiradagi foydalanuvchini
        // tozalaymiz. Busiz keyingi login eski foydalanuvchi ma'lumotini ko'rsatardi.
        scope.launch {
            preferencesManager.authorised.collect { authorised ->
                if (!authorised) clear()
            }
        }

        // Login paytida rol prefs'ga yoziladi, foydalanuvchi obyekti esa keyinroq
        // yuklanadi. Shu oraliqda rolni prefs'dan olamiz.
        // Foydalanuvchi obyekti kelgach, rol o'sha yerdan aniqlanadi (aniqroq manba),
        // shuning uchun bu yerda uni qayta yozmaymiz.
        scope.launch {
            preferencesManager.role.collect { roleName ->
                if (_user.value == null) {
                    _role.value = UserRole.fromString(roleName)
                }
            }
        }
    }

    fun setUser(user: UserDto?) {
        _user.value = user
        if (user == null) {
            _role.value = UserRole.UNKNOWN
            return
        }

        val determinedRole = UserRole.highestOf(user.roles?.map { it.name })
        _role.value = determinedRole
        scope.launch {
            preferencesManager.setRole(determinedRole.roleName)
        }
    }

    fun clear() {
        _user.value = null
        _role.value = UserRole.UNKNOWN
    }
}
