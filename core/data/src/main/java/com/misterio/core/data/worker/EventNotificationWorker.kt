package com.misterio.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.misterio.curakid.core.common.SettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class EventNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsManager: SettingsManager,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val isEnabled = settingsManager.isNotificationsEnabled.first()
        if (!isEnabled) {
            return Result.success()
        }

        val title = inputData.getString(KEY_EVENT_TITLE) ?: "Подія"
        val description = inputData.getString(KEY_EVENT_DESCRIPTION) ?: ""
        val eventId = inputData.getString(KEY_EVENT_ID)

        showNotification(title, description, eventId)

        return Result.success()
    }

    private fun showNotification(title: String, message: String, eventId: String?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Нагадування про події",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)?.apply {
            putExtra(EXTRA_EVENT_ID, eventId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = applicationContext.applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_dialog_info

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(smallIcon)
            .setColor(0xFF90CAF9.toInt())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(eventId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
            }
        } catch (e: SecurityException) {
            // Notification permission revoked or not yet granted by user
        }
    }

    companion object {
        const val CHANNEL_ID = "event_reminders_channel"
        const val KEY_EVENT_TITLE = "key_event_title"
        const val KEY_EVENT_DESCRIPTION = "key_event_description"
        const val KEY_EVENT_ID = "key_event_id"
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
