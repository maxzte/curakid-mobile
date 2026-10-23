package com.misterio.curakid.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.misterio.core.data.worker.EventNotificationWorker
import com.misterio.core.domain.repository.NotificationRepository
import com.misterio.core.domain.util.suspendRunCatching
import com.misterio.curakid.MainActivity
import com.misterio.curakid.core.common.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CurakidFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var settingsManager: SettingsManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            suspendRunCatching {
                notificationRepository.registerDeviceToken(token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        serviceScope.launch {
            val isEnabled = settingsManager.isNotificationsEnabled.first()
            if (!isEnabled) {
                return@launch
            }

            val title = remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: "CuraKid"
            val body = remoteMessage.notification?.body
                ?: remoteMessage.data["body"]
                ?: ""

            val eventId = remoteMessage.data[EventNotificationWorker.EXTRA_EVENT_ID]
                ?: remoteMessage.data["eventId"]
                ?: remoteMessage.data["event_id"]

            // Sync with local WorkManager: cancel redundant local reminder if server already delivered push
            if (!eventId.isNullOrBlank()) {
                try {
                    WorkManager.getInstance(applicationContext)
                        .cancelUniqueWork("event_notification_$eventId")
                } catch (e: Exception) {
                    // Safe fallback
                }
            }

            showNotification(title, body, remoteMessage.data, eventId)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        eventId: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                "CuraKid Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Сповіщення про вакцинацію, огляди та аналізи CuraKid"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val notificationId = eventId?.hashCode() ?: System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_dialog_info

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(smallIcon)
            .setColor(0xFF4CAF50.toInt())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val FCM_CHANNEL_ID = "curakid_fcm_channel"
    }
}

