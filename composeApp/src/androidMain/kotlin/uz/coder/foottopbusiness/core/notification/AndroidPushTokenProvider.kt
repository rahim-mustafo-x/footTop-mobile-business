package uz.coder.foottopbusiness.core.notification

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AndroidPushTokenProvider : PushTokenProvider {
    override suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            null
        }
    }
}