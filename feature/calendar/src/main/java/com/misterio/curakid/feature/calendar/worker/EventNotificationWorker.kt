package com.misterio.curakid.feature.calendar.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.misterio.curakid.feature.calendar.R

class EventNotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_EVENT_TITLE) ?: "Подія"
        val description = inputData.getString(KEY_EVENT_DESCRIPTION) ?: ""
        val eventId = inputData.getString(KEY_EVENT_ID)

        showNotification(title, description, eventId)

        return Result.success()
    }

    private fun showNotification(title: String, message: String, eventId: String?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Нагадування про події",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)?.apply {
            putExtra(EXTRA_EVENT_ID, eventId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            eventId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_event_notification)
            .setColor(0xFF4CAF50.toInt()) // CuraKid Primary Color (Greenish)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(eventId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "event_reminders_channel"
        const val KEY_EVENT_TITLE = "key_event_title"
        const val KEY_EVENT_DESCRIPTION = "key_event_description"
        const val KEY_EVENT_ID = "key_event_id"
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
