package uz.coder.foottopbusiness.core.notification

interface PushTokenProvider {
    suspend fun getToken(): String?
}