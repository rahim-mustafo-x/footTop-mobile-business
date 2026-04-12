package uz.coder.foottopbusiness.data.network

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.domain.usecase.notification.RegisterDeviceTokenUseCase

class FcmService : FirebaseMessagingService() {

    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase by inject()
    private val preferencesManager: PreferencesManager by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        
        scope.launch {
            val userId = preferencesManager.userId.first()
            if (userId != 0) {
                val request = DeviceTokenRequest(
                    userId = userId.toLong(),
                    token = token,
                    deviceType = "ANDROID"
                )
                registerDeviceTokenUseCase(request).collect { success ->
                    Log.d("FCM", "Token registration success: $success")
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received: ${message.notification?.title}")
    }
}
