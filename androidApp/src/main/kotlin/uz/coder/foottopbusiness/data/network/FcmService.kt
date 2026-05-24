package uz.coder.foottopbusiness.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import uz.coder.foottopbusiness.MainActivity
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.data.network.dto.notification.DeviceTokenRequest
import uz.coder.foottopbusiness.domain.usecase.notification.RegisterDeviceTokenUseCase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Suppress("DEPRECATION")
class FcmService : FirebaseMessagingService() {

    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase by inject()
    private val preferencesManager: PreferencesManager by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val CHANNEL_ID = "foottop_notifications"
        private const val CHANNEL_NAME = "FootTop Notifications"

        const val EXTRA_TYPE = "notification_type"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        const val ACTION_NOTIFICATION_OPENED = "uz.coder.foottopbusiness.NOTIFICATION_OPENED"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FcmService", "New token: $token")
        
        scope.launch {
            val userId = preferencesManager.userId.first()
            if (userId != 0) {
                val request = DeviceTokenRequest(
                    userId = userId.toLong(),
                    token = token,
                    deviceType = "ANDROID"
                )
                registerDeviceTokenUseCase(request).collect { success ->
                    Log.d("FcmService", "Token registration success: $success")
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FcmService", "Message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"]
        var body = message.notification?.body ?: message.data["body"]

        // ISO datetime formatlarni avtomatik formatlash
        body = body?.replace(
            Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}""")
        ) { matchResult ->
            formatDateTime(matchResult.value)
        }

        val type = message.data["type"]
        val notificationId = message.data["notificationId"]

        if (title != null && body != null) {
            val emojiTitle = getEmojiForType(type) + " " + title
            sendNotification(
                emojiTitle,
                body,
                type,
                notificationId
            )

            // Broadcast notification received for updating UI
            val broadcastIntent = Intent(ACTION_NOTIFICATION_OPENED).apply {
                putExtra(EXTRA_TYPE, type)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra("received", true)
            }
            sendBroadcast(broadcastIntent)
        }
    }

    private fun formatDateTime(dateTime: String?): String {
        return try {
            if (dateTime.isNullOrEmpty()) return ""
            val parsed = LocalDateTime.parse(dateTime)
            val formatter = DateTimeFormatter.ofPattern(
                "d-MMMM, HH:mm",
                Locale("uz")
            )
            parsed.format(formatter)
        } catch (_: Exception) {
            dateTime ?: ""
        }
    }

    private fun getEmojiForType(type: String?): String {
        return when (type) {
            "BOOKING" -> "📅"
            "MATCH" -> "⚽"
            "TOURNAMENT" -> "🏆"
            "SYSTEM" -> "📢"
            "BOOKING_CONFIRMED" -> "✅"
            "BOOKING_CANCELLED" -> "❌"
            "MATCH_JOINED" -> "🙋‍♂️"
            "MATCH_FULL" -> "🔥"
            "MATCH_PLAYER_LEFT" -> "🏃"
            "BOOKING_REMINDER" -> "⏰"
            "MATCH_REMINDER" -> "⏳"
            "TOURNAMENT_REMINDER" -> "🎗️"
            else -> "🔔"
        }
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        type: String?,
        notificationId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(
            android.media.RingtoneManager.TYPE_NOTIFICATION
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "FootTop Notifications Channel"
            enableLights(true)
            lightColor = android.graphics.Color.GREEN
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationIdInt = notificationId?.toIntOrNull() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationIdInt, notificationBuilder.build())
    }
}
