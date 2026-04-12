package uz.coder.foottopbusiness.core.notification

class IosPushTokenProvider : PushTokenProvider {
    override suspend fun getToken(): String? = null
}